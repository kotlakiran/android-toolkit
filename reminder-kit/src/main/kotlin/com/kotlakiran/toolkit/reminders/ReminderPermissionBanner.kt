package com.kotlakiran.toolkit.reminders

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.kotlakiran.toolkit.coreui.theme.LocalAppTokens

/**
 * Shows when notifications are OFF for the app, re-checking on resume.
 * Tapping opens the app's system notification settings — the only reliable
 * path once the user has permanently denied the runtime permission dialog
 * (it only shows once; never auto-dismiss the user's "no" without a way back).
 */
@Composable
fun ReminderPermissionBanner(
    modifier: Modifier = Modifier,
    text: String = "Reminders are off — tap to enable in system settings",
) {
    val t = LocalAppTokens.current
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(notificationsEnabled(context)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) enabled = notificationsEnabled(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (!enabled) {
        Row(
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(t.warn.copy(alpha = 0.12f))
                .border(1.dp, t.warn.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                .clickable { openNotificationSettings(context) }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("⚠️", fontSize = 13.sp)
            Text(text, color = t.ink, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun notificationsEnabled(context: Context): Boolean =
    NotificationManagerCompat.from(context).areNotificationsEnabled()

private fun openNotificationSettings(context: Context) {
    context.startActivity(
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}
