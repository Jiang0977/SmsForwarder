package com.idormy.sms.forwarder

import com.idormy.sms.forwarder.utils.NetworkSecurity
import com.idormy.sms.forwarder.utils.PrivacyLog
import org.junit.Assert.*
import org.junit.Test
import java.security.Principal
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLSession
import javax.net.ssl.SSLSessionContext

class SecurityHardeningTest {
    @Test fun hostnameMustMatchThePresentedCertificate() {
        val certificate = javaClass.getResourceAsStream("/localhost.pem").use {
            CertificateFactory.getInstance("X.509").generateCertificate(it)
        }
        val session = CertificateSession(certificate)
        assertTrue(NetworkSecurity.hostnameVerifier.verify("localhost", session))
        assertFalse(NetworkSecurity.hostnameVerifier.verify("unexpected.example", session))
        assertFalse(NetworkSecurity.hostnameVerifier.verify("127.0.0.1", session))
    }

    @Test fun credentialsAndNestedPayloadsAreOmitted() {
        val secrets = listOf(
            "Rule(sender=Sender(jsonSetting={\"token\":\"test-token\",\"secret\":\"test-secret\"}))",
            "debug payload={\"nested\":{\"access_token\":\"test-token\"}}",
            "Authorization: Bearer test-token",
            "from = +8613800000000, msg = private SMS\nsecond line",
            "msgInfo = MsgInfo(content=private SMS)",
            "{\"data\":\"private SMS\"}",
            "requestMsg: private SMS",
            "tagRegex: regex=[0-9], replacement=x, temp=private SMS",
            "item: LogsDetail(rule=private SMS)"
        )
        secrets.forEach { message ->
            val safe = PrivacyLog.redact("SmsReceiver", message)
            listOf("test-token", "test-secret", "private SMS", "13800000000").forEach {
                assertFalse(safe, safe.contains(it))
            }
        }
    }

    @Test fun unlabelledProviderBodiesAndCloneDataAreOmitted() {
        assertFalse(PrivacyLog.redact("DingtalkGroupRobotUtils", "private SMS").contains("private SMS"))
        assertFalse(PrivacyLog.redact("CloneController", "private SMS").contains("private SMS"))
    }

    @Test fun URLsCannotLeakCredentialsThroughExceptionMessages() {
        val safe = PrivacyLog.redact("Transport", "Failed for https://user:pass@example.test/bot/test-token?secret=test-secret")
        listOf("user:pass", "test-token", "test-secret").forEach { assertFalse(safe.contains(it)) }
    }

    @Test fun forwardHistoryKeepsOnlyStatusAndBoundedProviderCodes() {
        val safe = PrivacyLog.forwardingResponse(2, "{\"errcode\":0,\"errmsg\":\"private SMS test-token\"}")
        assertEquals("Forwarding succeeded (errcode=0)", safe)
        assertEquals(safe, PrivacyLog.forwardingResponse(2, safe))
        assertEquals("Forwarding failed", PrivacyLog.forwardingResponse(0, "private SMS test-token"))
        assertFalse(PrivacyLog.forwardingResponse(0, "code=13800000000").contains("13800000000"))
    }

    @Test fun onlyGeneratedHttpMetadataIsAcceptedAsDiagnosticDetail() {
        val safe = "HTTP request: POST host=oapi.dingtalk.com tls=true"
        assertEquals(safe, PrivacyLog.forwardingResponse(-1, safe))
        assertEquals(safe, PrivacyLog.redact("LoggingInterceptor", safe))
        val response = "HTTP response: code=200 elapsedMs=12"
        assertEquals(response, PrivacyLog.forwardingResponse(-1, response))
        assertEquals(response, PrivacyLog.redact("LoggingInterceptor", response))
        assertFalse(PrivacyLog.forwardingResponse(-1, "$safe\nprivate SMS").contains("private SMS"))
        assertFalse(PrivacyLog.forwardingResponse(-1, "body: private SMS").contains("private SMS"))
    }

    private class CertificateSession(private val certificate: Certificate) : SSLSession {
        override fun getPeerCertificates() = arrayOf(certificate)
        override fun getLocalCertificates(): Array<Certificate>? = null
        @Suppress("DEPRECATION")
        override fun getPeerCertificateChain(): Array<javax.security.cert.X509Certificate> = emptyArray()
        override fun getPeerPrincipal(): Principal? = null
        override fun getLocalPrincipal(): Principal? = null
        override fun getId() = byteArrayOf()
        override fun getSessionContext(): SSLSessionContext? = null
        override fun getCreationTime() = 0L
        override fun getLastAccessedTime() = 0L
        override fun invalidate() {}
        override fun isValid() = true
        override fun putValue(name: String, value: Any) {}
        override fun getValue(name: String): Any? = null
        override fun removeValue(name: String) {}
        override fun getValueNames(): Array<String> = emptyArray()
        override fun getCipherSuite() = "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"
        override fun getProtocol() = "TLSv1.2"
        override fun getPeerHost() = "localhost"
        override fun getPeerPort() = 443
        override fun getPacketBufferSize() = 16384
        override fun getApplicationBufferSize() = 16384
    }
}
