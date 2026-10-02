package com.kotlakiran.toolkit.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

data class ReminderChannel(
    val channelId: String,
    val name: String,
    val importance: Int = NotificationManager.IMPORTANCE_HIGH,
)

object NotificationChannels {

    /**
     * Channel importance and sound are FROZEN at first creation — to change
     * them later, bump the channel id (e.g. "reminders" -> "reminders_v2"),
     * and optionally delete the old one here.
     */
    fun ensure(context: Context, channel: ReminderChannel, replaceLegacyId: String? = null) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (replaceLegacyId != null && replaceLegacyId != channel.channelId) {
            manager.deleteNotificationChannel(replaceLegacyId)
        }
        manager.createNotificationChannel(
            NotificationChannel(channel.channelId, channel.name, channel.importance)
        )
    }
}
