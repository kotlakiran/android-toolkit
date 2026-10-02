package com.kotlakiran.toolkit.coreui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotlakiran.toolkit.coreui.theme.LocalAppTokens

enum class Tone { NEUTRAL, GOOD, WARN, BAD, ACCENT }

data class AppTab(val id: String, val label: String, val glyph: String)

@Composable
fun SectionCard(
    title: String? = null,
    action: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val t = LocalAppTokens.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = t.card,
        border = BorderStroke(1.dp, t.line),
    ) {
        Column(Modifier.padding(13.dp)) {
            if (title != null) {
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(title, color = t.ink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    if (action != null) {
                        Text(
                            action,
                            color = t.accent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onAction?.invoke() }.padding(4.dp),
                        )
                    }
                }
            }
            content()
        }
    }
}

@Composable
fun StatTile(label: String, value: String, tone: Tone = Tone.NEUTRAL, modifier: Modifier = Modifier) {
    val t = LocalAppTokens.current
    val valueColor = when (tone) {
        Tone.GOOD -> t.good
        Tone.WARN -> t.warn
        Tone.BAD -> t.bad
        Tone.ACCENT -> t.accent
        Tone.NEUTRAL -> t.ink
    }
    Column(
        modifier
            .clip(RoundedCornerShape(13.dp))
            .background(t.card.copy(alpha = 0.6f))
            .border(1.dp, t.line, RoundedCornerShape(13.dp))
            .padding(9.dp)
    ) {
        Text(label.uppercase(), color = t.muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.7.sp)
        Text(value, color = valueColor, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
    }
}

/** Progress bar with an optional threshold tick (e.g. EMI share vs comfort line). */
@Composable
fun MeterBar(fraction: Float, modifier: Modifier = Modifier, markFraction: Float? = null) {
    val t = LocalAppTokens.current
    BoxWithConstraints(modifier.fillMaxWidth().height(10.dp)) {
        val w = maxWidth
        Box(Modifier.fillMaxSize().clip(RoundedCornerShape(99.dp)).background(t.ink.copy(alpha = 0.08f)))
        Box(
            Modifier
                .width(w * fraction.coerceIn(0f, 1f))
                .height(10.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(t.accent)
        )
        if (markFraction != null) {
            Box(
                Modifier
                    .padding(start = (w * markFraction.coerceIn(0f, 1f)) - 1.dp)
                    .width(2.dp)
                    .height(14.dp)
                    .align(Alignment.CenterStart)
                    .background(t.ink.copy(alpha = 0.6f))
            )
        }
    }
}

@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = LocalAppTokens.current
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(t.fieldBg)
            .border(1.dp, t.line, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEachIndexed { i, opt ->
            val on = i == selectedIndex
            val bg by animateColorAsState(if (on) t.ink else Color.Transparent, label = "seg")
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(bg)
                    .clickable { onSelect(i) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(opt, color = if (on) t.bg else t.dim, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AppListRow(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val t = LocalAppTokens.current
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(t.ink.copy(alpha = 0.03f))
            .border(1.dp, t.line, RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = t.ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            if (subtitle != null) Text(subtitle, color = t.muted, fontSize = 10.5.sp)
        }
        trailing?.invoke()
    }
}

/** Floating rounded tab bar; place with Modifier.align(Alignment.BottomCenter) in a Box. */
@Composable
fun FloatingBottomBar(
    tabs: List<AppTab>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = LocalAppTokens.current
    Surface(
        modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
        shape = RoundedCornerShape(26.dp),
        color = t.card,
        border = BorderStroke(1.dp, t.line),
        shadowElevation = 12.dp,
    ) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            tabs.forEach { tab ->
                val on = tab.id == selectedId
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelect(tab.id) }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(tab.glyph, color = if (on) t.accent else t.dim, fontSize = 16.sp)
                    Text(tab.label, color = if (on) t.ink else t.dim, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
