package com.idormy.sms.forwarder.utils

import okhttp3.OkHttpClient
import javax.net.ssl.HostnameVerifier

/** Use OkHttp's strict hostname verifier instead of XHttp's permissive default. */
object NetworkSecurity {
    const val allowRemoteHttpServer = false
    const val allowDynamicFrpc = false

    val hostnameVerifier: HostnameVerifier = OkHttpClient().hostnameVerifier()
}
