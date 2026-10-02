package com.kotlakiran.toolkit.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotlakiran.toolkit.coreui.theme.LocalAppTokens

/**
 * Grid goal picker, the first-run screen used across apps. The app supplies
 * goals + a continue handler; selected ids come back in onContinue.
 */
@Composable
fun GoalPickerScreen(
    title: String,
    subtitle: String,
    goals: List<GoalOption>,
    onContinue: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
    continueLabel: String = "Continue",
) {
    val t = LocalAppTokens.current
    var selected by remember { mutableStateOf(setOf<String>()) }

    Column(modifier.fillMaxSize().padding(horizontal = 18.dp).padding(top = 40.dp, bottom = 18.dp)) {
        Text(title, color = t.ink, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Text(subtitle, color = t.muted, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 18.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(goals) { goal ->
                val on = goal.id in selected
                Column(
                    Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (on) t.accent.copy(alpha = 0.16f) else t.card)
                        .border(1.dp, if (on) t.accent else t.line, RoundedCornerShape(16.dp))
                        .clickable {
                            selected = if (on) selected - goal.id else selected + goal.id
                        }
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (goal.emoji.isNotEmpty()) Text(goal.emoji, fontSize = 26.sp)
                    Text(
                        goal.label,
                        color = if (on) t.ink else t.dim,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (selected.isEmpty()) t.fieldBg else t.accent)
                .clickable(enabled = selected.isNotEmpty()) { onContinue(selected) }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                continueLabel,
                color = if (selected.isEmpty()) t.muted else t.ink,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
