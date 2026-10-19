package com.idormy.sms.forwarder.utils

import android.content.Context
import android.os.UserManager
import android.os.Build
import com.idormy.sms.forwarder.core.Core
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/** One-time cleanup keeps messages, forwarding statuses, rules and channel settings. */
object PrivacyCleanup {
    private val running = AtomicBoolean(false)
    fun run(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
            if (!userManager.isUserUnlocked) return
        }
        val storage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) context.createDeviceProtectedStorageContext() else context
        val prefs = storage.getSharedPreferences("privacy_hardening", Context.MODE_PRIVATE)
        if (prefs.getBoolean("diagnostics_cleaned_v1", false) || !running.compareAndSet(false, true)) return
        Thread {
            try {
                Core.logs.clearLegacyResponses()
                val stores = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    listOf(context, context.createDeviceProtectedStorageContext())
                } else listOf(context)
                stores.forEach { store ->
                    File(store.cacheDir, "logs").listFiles()?.filter { it.isFile }?.forEach {
                        check(it.delete() || !it.exists())
                    }
                    listOf("ua.db", "umeng_zero_cache.db").forEach { store.deleteDatabase(it) }
                    listOf("um_session_id", "umeng_sp_oaid", "UM_PROBE_DATA", "umeng_common_config",
                        "um_policy_grant", "umzid_general_config", "umeng_general_config").forEach {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) store.deleteSharedPreferences(it)
                        else File(store.applicationInfo.dataDir, "shared_prefs/$it.xml").delete()
                    }
                }
                check(prefs.edit().putBoolean("diagnostics_cleaned_v1", true).commit())
                Log.i("PrivacyCleanup", "Legacy diagnostic data cleared")
            } catch (error: Exception) {
                // Keep the flag unset so an incomplete cleanup can retry on the next launch.
                Log.e("PrivacyCleanup", "Cleanup failed: ${error.javaClass.simpleName}")
            } finally {
                running.set(false)
            }
        }.start()
    }
}
