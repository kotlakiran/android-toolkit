package com.kotlakiran.toolkit.notifcapture

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotlakiran.toolkit.coreui.theme.LocalAppTokens

data class BankAppOption(val packageName: String, val label: String)

/** Common Indian bank / payment apps the user can pick from. */
val KNOWN_BANK_APPS = listOf(
    BankAppOption("com.sbi.lotusintouch", "SBI YONO"),
    BankAppOption("com.snapwork.hdfc", "HDFC Bank"),
    BankAppOption("com.csam.icici.bank.imobile", "ICICI iMobile"),
    BankAppOption("com.axis.mobile", "Axis Mobile"),
    BankAppOption("com.mgs.kotak", "Kotak 811"),
    BankAppOption("com.google.android.apps.nbu.paisa.user", "GPay"),
    BankAppOption("com.phonepe.app", "PhonePe"),
    BankAppOption("net.one97.paytm", "Paytm"),
)

/**
 * Play-safe disclosure shown BEFORE sending the user to system settings.
 * The decline path must keep the app fully usable (e.g. PDF import) — that is
 * a Play restricted-permission requirement, not just politeness.
 *
 * Wrap this in your own sheet/dialog; onContinue receives the picked packages.
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun NotificationConsentContent(
    options: List<BankAppOption>,
    onContinue: (selected: Set<String>) -> Unit,
    onNotNow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = LocalAppTokens.current
    var selected by remember { mutableStateOf(setOf<String>()) }

    Column(modifier.fillMaxWidth().padding(4.dp)) {
        Text("Allow reading bank notifications?", color = t.ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(
            "So new EMIs and spends show up without uploading a PDF each month.",
            color = t.muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp),
        )

        Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Bullet(t, good = true, text = "Only notifications from apps you select")
            Bullet(t, good = true, text = "Only amount, merchant, date and type are kept")
            Bullet(t, good = true, text = "Processed on your phone. Never sold or used for ads")
            Bullet(t, good = true, text = "Turn off anytime in Settings")
            Bullet(t, good = false, text = "No SMS, chats, or OTPs")
            Bullet(t, good = false, text = "No notifications from other apps")
        }

        Text(
            "Choose apps",
            color = t.muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.7.sp,
            modifier = Modifier.padding(top = 14.dp, bottom = 6.dp),
        )
        androidx.compose.foundation.layout.FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            options.forEach { opt ->
                val on = opt.packageName in selected
                Box(
                    Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(if (on) t.accent.copy(alpha = 0.18f) else t.fieldBg)
                        .border(1.dp, if (on) t.accent else t.line, RoundedCornerShape(99.dp))
                        .clickable {
                            selected = if (on) selected - opt.packageName else selected + opt.packageName
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        (if (on) "✓ " else "") + opt.label,
                        color = if (on) t.ink else t.dim,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        Box(
            Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(13.dp))
                .background(t.accent)
                .clickable(enabled = selected.isNotEmpty()) { onContinue(selected) }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("Continue to Android settings", color = t.ink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Box(
            Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(13.dp))
                .background(t.fieldBg)
                .border(1.dp, t.line, RoundedCornerShape(13.dp))
                .clickable { onNotNow() }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("Not now, I'll use PDFs", color = t.ink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun Bullet(t: com.kotlakiran.toolkit.coreui.theme.AppTokens, good: Boolean, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
        Text(if (good) "✓" else "✕", color = if (good) t.good else t.bad, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(text, color = t.ink, fontSize = 11.5.sp, lineHeight = 16.sp)
    }
}
