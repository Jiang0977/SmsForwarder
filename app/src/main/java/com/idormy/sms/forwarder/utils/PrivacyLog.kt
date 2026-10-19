package com.idormy.sms.forwarder.utils

/** Shared boundary for logcat, file logs and persisted forwarding diagnostics. */
object PrivacyLog {
    private const val OMITTED = "[private payload omitted]"
    private val payloadTags = setOf(
        "PhoneUtils",
        "MsgInfo",
        "SmsCommandUtils",
        "ConditionUtils",
        "BarkUtils",
        "DingtalkGroupRobotUtils",
        "DingtalkInnerRobotUtils",
        "EmailUtils",
        "FeishuAppApi",
        "FeishuAppUtils",
        "FeishuUtils",
        "GotifyUtils",
        "PushplusUtils",
        "ServerchanUtils",
        "SmsUtils",
        "SocketUtils",
        "TelegramUtils",
        "UrlSchemeUtils",
        "WebhookUtils",
        "WeworkAgentUtils",
        "WeworkRobotUtils"
    )
    private val payload = Regex(
        """(?i)(?:\b(?:from|msg|message|content|text|title|body|item|regex|replacement|temp|command|cmd|json|bean|param|response|requestMsg|requestUrl|msgInfo|rule|sender|task|setting|token|access_token|secret|password|authorization|safePhone|phoneNumber|savedNumber|number|address|privateKey|publicKey|sign)\s*[=:：]|\b(?:MsgInfo|SimInfo|CallInfo|ContactInfo|CloneInfo|Rule|Sender|Task|\w+Setting|\w+Request|\w+Response)\(|\bBearer\s)"""
    )
    private val credentials = Regex("(?i)(token|secret|password|authorization|privateKey)")
    private val urls = Regex("""(?i)(?:https?|wss?|tcp|udp)://[^\s<>]+""")
    private val phones = Regex("""(?<![\w])\+?\d[\d -]{6,}\d(?![\w])""")
    private val emails = Regex("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}""")
    private val code = Regex("""(?i)["']?(errcode|error_code|code|status)["']?\s*[:=]\s*(-?\d{1,6})(?!\d)""")
    private val httpMetadata = Regex("""HTTP (?:request: (?:GET|POST|PUT|PATCH|DELETE|HEAD|OPTIONS) host=[A-Za-z0-9.:_-]+ tls=(?:true|false)|response: code=\d{3} elapsedMs=\d+|transport failure: [A-Za-z0-9_]+)""")

    fun redact(tag: String, message: String): String {
        if (httpMetadata.matches(message)) return message
        if (tag in payloadTags || tag.endsWith("Controller") || payload.containsMatchIn(message) || credentials.containsMatchIn(message)
            || message.trimStart().startsWith("{") || message.trimStart().startsWith("[")) {
            return OMITTED
        }
        return emails.replace(phones.replace(urls.replace(message, "[URL omitted]"), "[number omitted]"), "[email omitted]")
    }

    /** Never persist raw response bodies, headers, request URLs or provider error text. */
    fun forwardingResponse(status: Int, response: String): String {
        if (status < 0) return if (httpMetadata.matches(response)) response else OMITTED
        val label = if (status == 2) "Forwarding succeeded" else "Forwarding failed"
        val codes = code.findAll(response).map { "${it.groupValues[1].lowercase()}=${it.groupValues[2]}" }.distinct().take(4).toList()
        return if (codes.isEmpty()) label else "$label (${codes.joinToString(", ")})"
    }
}
