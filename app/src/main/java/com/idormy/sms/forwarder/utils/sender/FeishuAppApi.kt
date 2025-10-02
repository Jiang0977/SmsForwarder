package com.idormy.sms.forwarder.utils.sender

import android.text.TextUtils
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.idormy.sms.forwarder.utils.Log
import com.idormy.sms.forwarder.utils.SettingUtils
import com.idormy.sms.forwarder.utils.SharedPreference
import com.xuexiang.xhttp2.XHttp
import com.xuexiang.xhttp2.callback.SimpleCallBack
import com.xuexiang.xhttp2.exception.ApiException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

// 飞书企业应用相关 API 封装（已读查询、加急短信/电话）
object FeishuAppApi {

    private const val TAG: String = "FeishuAppApi"

    // 查询消息已读用户，返回 user_id 列表
    suspend fun getMessageReadUserIds(appId: String, messageId: String): Result<List<String>> {
        val accessToken: String by SharedPreference("feishu_access_token_" + appId, "")
        val url = "https://open.feishu.cn/open-apis/im/v1/messages/$messageId/read_users?user_id_type=user_id"
        Log.d(TAG, "getMessageReadUserIds url=$url")

        return suspendCancellableCoroutine { cont ->
            XHttp.get(url)
                .headers("Authorization", "Bearer $accessToken")
                .timeStamp(true)
                .retryCount(SettingUtils.requestRetryTimes)
                .retryDelay(SettingUtils.requestDelayTime * 1000)
                .retryIncreaseDelay(SettingUtils.requestDelayTime * 1000)
                .keepJson(true)
                .execute(object : SimpleCallBack<String>() {
                    override fun onError(e: ApiException) {
                        Log.e(TAG, e.detailMessage)
                        cont.resume(Result.failure(Throwable(e.displayMessage)))
                    }

                    override fun onSuccess(response: String) {
                        try {
                            val ids = mutableListOf<String>()
                            val root = JsonParser.parseString(response).asJsonObject
                            val code = root.get("code")?.asLong ?: -1L
                            if (code != 0L) {
                                cont.resume(Result.failure(Throwable("code=$code")))
                                return
                            }
                            val data = root.getAsJsonObject("data")
                            val items = data?.getAsJsonArray("items")
                            items?.forEach { el ->
                                val item = el.asJsonObject
                                val uid = item.get("user_id")?.asString
                                if (!uid.isNullOrEmpty()) ids.add(uid)
                            }
                            cont.resume(Result.success(ids))
                        } catch (e: Exception) {
                            cont.resume(Result.failure(e))
                        }
                    }
                })
        }
    }

    // 调用加急短信/电话（user_id_type 固定 user_id）
    suspend fun sendUrgentSms(appId: String, userId: String, messageId: String, uuid: String? = null): Result<Unit> {
        val accessToken: String by SharedPreference("feishu_access_token_" + appId, "")
        val url = "https://open.feishu.cn/open-apis/im/v1/messages/$messageId/urgent_phone?user_id_type=user_id"
        val body = mutableMapOf<String, Any>(
            "user_id_list" to listOf(userId),
        )
        val requestMsg = Gson().toJson(body)
        Log.d(TAG, "sendUrgentPhone url=$url body=$requestMsg")

        return suspendCancellableCoroutine { cont ->
            XHttp.patch(url)
                .headers("Authorization", "Bearer $accessToken")
                .upJson(requestMsg)
                .keepJson(true)
                .timeStamp(true)
                .retryCount(SettingUtils.requestRetryTimes)
                .retryDelay(SettingUtils.requestDelayTime * 1000)
                .retryIncreaseDelay(SettingUtils.requestDelayTime * 1000)
                .execute(object : SimpleCallBack<String>() {
                    override fun onError(e: ApiException) {
                        Log.e(TAG, e.detailMessage)
                        cont.resume(Result.failure(Throwable(e.displayMessage)))
                    }

                    override fun onSuccess(response: String) {
                        try {
                            val root = JsonParser.parseString(response).asJsonObject
                            val code = root.get("code")?.asLong ?: -1L
                            if (code == 0L) {
                                cont.resume(Result.success(Unit))
                            } else {
                                cont.resume(Result.failure(Throwable("code=$code")))
                            }
                        } catch (e: Exception) {
                            cont.resume(Result.failure(e))
                        }
                    }
                })
        }
    }
}



