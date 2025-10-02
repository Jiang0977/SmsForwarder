package com.idormy.sms.forwarder.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.idormy.sms.forwarder.utils.Log
import com.idormy.sms.forwarder.utils.sender.FeishuAppApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.TimeUnit

class FeishuUrgentWorker(private val appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    private val TAG: String = FeishuUrgentWorker::class.java.simpleName

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val appId = inputData.getString(KEY_APP_ID)
        val userId = inputData.getString(KEY_USER_ID)
        val messageId = inputData.getString(KEY_MESSAGE_ID)
        val maxAttempts = inputData.getInt(KEY_MAX_ATTEMPTS, 3)
        val attemptIndex = inputData.getInt(KEY_ATTEMPT_INDEX, 0)
        val initialDelay = inputData.getLong(KEY_INITIAL_DELAY_SECONDS, 60L)

        if (appId.isNullOrEmpty() || userId.isNullOrEmpty() || messageId.isNullOrEmpty()) {
            Log.e(TAG, "invalid input data")
            return@withContext Result.failure()
        }

        try {
            // 1. 查询是否已读
            val readUsers = FeishuAppApi.getMessageReadUserIds(appId, messageId)
            if (readUsers.isSuccess) {
                if (readUsers.getOrThrow().contains(userId)) {
                    Log.i(TAG, "message $messageId already read by $userId, stop")
                    return@withContext Result.success()
                }
            } else {
                Log.e(TAG, "read_users error: ${readUsers.exceptionOrNull()?.message}")
            }

            // 2. 未读，若尝试次数已达上限则结束
            if (attemptIndex >= maxAttempts) {
                Log.i(TAG, "reach max attempts, stop. messageId=$messageId")
                return@withContext Result.success()
            }

            // 3. 调用加急（失败不计数）
            val urgentResult = FeishuAppApi.sendUrgentSms(appId, userId, messageId, UUID.randomUUID().toString())
            val nextAttemptIndex = if (urgentResult.isSuccess) attemptIndex + 1 else attemptIndex

            // 4. 指数退避计算下次延时：initial * 2^nextAttemptIndex
            val delaySeconds = initialDelay * (1L shl nextAttemptIndex)
            schedule(
                appContext = appContext,
                appId = appId,
                userId = userId,
                messageId = messageId,
                maxAttempts = maxAttempts,
                attemptIndex = nextAttemptIndex,
                initialDelaySeconds = initialDelay,
                delaySeconds = delaySeconds
            )

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "worker error: ${e.message}")
            Result.retry()
        }
    }

    companion object {
        private const val KEY_APP_ID = "appId"
        private const val KEY_USER_ID = "userId"
        private const val KEY_MESSAGE_ID = "messageId"
        private const val KEY_MAX_ATTEMPTS = "maxAttempts"
        private const val KEY_ATTEMPT_INDEX = "attemptIndex"
        private const val KEY_INITIAL_DELAY_SECONDS = "initialDelaySeconds"

        fun schedule(
            appContext: Context,
            appId: String,
            userId: String,
            messageId: String,
            maxAttempts: Int,
            initialDelaySeconds: Long,
            attemptIndex: Int = 0,
            delaySeconds: Long = initialDelaySeconds,
        ) {
            val data = workDataOf(
                KEY_APP_ID to appId,
                KEY_USER_ID to userId,
                KEY_MESSAGE_ID to messageId,
                KEY_MAX_ATTEMPTS to maxAttempts,
                KEY_ATTEMPT_INDEX to attemptIndex,
                KEY_INITIAL_DELAY_SECONDS to initialDelaySeconds,
            )
            val request = OneTimeWorkRequestBuilder<FeishuUrgentWorker>()
                .setInputData(data)
                .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
                .addTag("feishu_urgent_$messageId")
                .build()
            WorkManager.getInstance(appContext).enqueue(request)
        }
    }
}



