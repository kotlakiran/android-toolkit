package com.kotlakiran.toolkit.reminders

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Base reminder worker: posts a notification on the app-provided channel and
 * reschedules the next occurrence via the handler the app registered.
 *
 * The notification permission is checked before posting and a denial is
 * surfaced through [onBlocked] so the app can show the banner instead of
 * failing silently (the classic "no notifications, no feedback" bug).
 */
open class ReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    final override suspend fun doWork(): Result {
        val tag = inputData.getString(KEY_TAG) ?: return Result.failure()
        val handler = handlers[tag] ?: return Result.failure()
        val payload = handler.onFire(applicationContext, inputData)
        val canPost = ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED &&
            NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()

        if (canPost) {
            NotificationChannels.ensure(applicationContext, payload.channel)
            val launch = applicationContext.packageManager
                .getLaunchIntentForPackage(applicationContext.packageName)
                ?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val tap = launch?.let {
                PendingIntent.getActivity(
                    applicationContext, payload.id, it,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            }
            val notification = NotificationCompat.Builder(applicationContext, payload.channel.channelId)
                .setSmallIcon(payload.smallIconRes)
                .setContentTitle(payload.title)
                .setContentText(payload.body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .apply { tap?.let(::setContentIntent) }
                .build()
            NotificationManagerCompat.from(applicationContext).notify(payload.id, notification)
        } else {
            handler.onBlocked(applicationContext, tag)
        }
        handler.rescheduleNext(applicationContext, tag, inputData)
        return Result.success()
    }

    companion object {
        const val KEY_TAG = "toolkit.reminder.tag"

        private val handlers = HashMap<String, ReminderHandler>()

        /** App registers one handler per reminder tag at startup. */
        fun register(tag: String, handler: ReminderHandler) {
            handlers[tag] = handler
        }
    }
}

data class ReminderPayload(
    val id: Int,
    val channel: ReminderChannel,
    val smallIconRes: Int,
    val title: String,
    val body: String,
)

interface ReminderHandler {
    fun onFire(context: Context, data: androidx.work.Data): ReminderPayload

    /** Called instead of posting when notification permission is denied. */
    fun onBlocked(context: Context, tag: String) {}

    fun rescheduleNext(context: Context, tag: String, data: androidx.work.Data)
}
