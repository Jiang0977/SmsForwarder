package com.idormy.sms.forwarder.utils.interceptor

import com.idormy.sms.forwarder.utils.Log
import com.idormy.sms.forwarder.utils.SendUtils
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Log transport metadata only. Message bodies and credentials never enter diagnostics. */
class LoggingInterceptor(private val logId: Long) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url()
        log("HTTP request: ${request.method()} host=${url.host()} tls=${url.isHttps}")
        val startedAt = System.nanoTime()
        return try {
            val response = chain.proceed(request)
            val elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt)
            log("HTTP response: code=${response.code()} elapsedMs=$elapsedMs")
            response
        } catch (error: IOException) {
            log("HTTP transport failure: ${error.javaClass.simpleName}")
            throw error
        }
    }

    private fun log(message: String) {
        Log.d("LoggingInterceptor", message)
        SendUtils.updateLogs(logId, -1, message)
    }
}
