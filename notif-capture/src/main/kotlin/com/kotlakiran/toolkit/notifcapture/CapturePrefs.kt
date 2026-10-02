package com.kotlakiran.toolkit.notifcapture

import android.content.Context

/**
 * In-app consent + allowlist for notification capture. The listener ignores
 * everything unless the user granted consent in the app first — Android's
 * system-level notification access alone is not treated as consent.
 */
object CapturePrefs {
    private const val FILE = "toolkit_notif_capture"
    private const val KEY_CONSENT_AT = "consent_at"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_PACKAGES = "packages"

    private fun prefs(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun grantConsent(context: Context, packages: Set<String>) {
        prefs(context).edit()
            .putLong(KEY_CONSENT_AT, System.currentTimeMillis())
            .putBoolean(KEY_ENABLED, true)
            .putStringSet(KEY_PACKAGES, packages)
            .apply()
    }

    fun setPackages(context: Context, packages: Set<String>) {
        prefs(context).edit().putStringSet(KEY_PACKAGES, packages).apply()
    }

    fun revoke(context: Context) {
        prefs(context).edit().putBoolean(KEY_ENABLED, false).apply()
    }

    fun isEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ENABLED, false) && prefs(context).getLong(KEY_CONSENT_AT, 0L) > 0L

    fun allowedPackages(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_PACKAGES, emptySet()) ?: emptySet()
}
