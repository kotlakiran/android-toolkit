package com.kotlakiran.toolkit.notifcapture

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableSharedFlow

data class CapturedAlert(
    val packageName: String,
    val title: String,
    val text: String,
    val postedAt: Long,
)

/** Apps collect this flow and turn alerts into their own transaction model. */
object CaptureBus {
    val alerts = MutableSharedFlow<CapturedAlert>(extraBufferCapacity = 64)
}

/**
 * Reads notifications only when the user granted in-app consent (CapturePrefs)
 * AND Android notification access, only from user-picked packages, and skips
 * anything that looks like an OTP. Android 15 additionally redacts OTP
 * notification content from listeners, so OTPs must never be expected here.
 */
class BankNotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val ctx = applicationContext
        if (!CapturePrefs.isEnabled(ctx)) return
        if (sbn.packageName !in CapturePrefs.allowedPackages(ctx)) return
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val plain = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val big = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
        val body = if (big.isNotBlank()) big else plain
        if (body.isBlank() || looksSensitive(body)) return
        CaptureBus.alerts.tryEmit(CapturedAlert(sbn.packageName, title, body, sbn.postTime))
    }

    private fun looksSensitive(text: String): Boolean {
        val t = text.lowercase()
        return t.contains("otp") ||
            t.contains("one time") ||
            t.contains("one-time") ||
            t.contains("verification code") ||
            t.contains("passcode")
    }
}
