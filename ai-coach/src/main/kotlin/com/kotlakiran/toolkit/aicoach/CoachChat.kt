package com.kotlakiran.toolkit.aicoach

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotlakiran.toolkit.coreui.theme.LocalAppTokens

data class ChatMessage(
    val fromUser: Boolean,
    val text: String,
    /** e.g. "Sources: HDFC_Sep_2026.pdf · 3 loans" shown under assistant replies. */
    val sources: String? = null,
)

/** Stateless chat UI; the app owns the message list and calls AiCoach. */
@Composable
fun CoachChat(
    messages: List<ChatMessage>,
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier,
    quickChips: List<String> = emptyList(),
    thinking: Boolean = false,
    placeholder: String = "Ask anything…",
) {
    val t = LocalAppTokens.current
    var draft by remember { mutableStateOf("") }

    fun send() {
        val q = draft.trim()
        if (q.isNotEmpty()) {
            draft = ""
            onSend(q)
        }
    }

    Column(modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp),
        ) {
            items(messages) { msg ->
                val mine = msg.fromUser
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = if (mine) Alignment.End else Alignment.Start,
                ) {
                    Box(
                        Modifier
                            .widthIn(max = 300.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 14.dp,
                                    topEnd = 14.dp,
                                    bottomStart = if (mine) 14.dp else 4.dp,
                                    bottomEnd = if (mine) 4.dp else 14.dp,
                                )
                            )
                            .background(if (mine) t.accent else t.card)
                            .then(if (mine) Modifier else Modifier.border(1.dp, t.line, RoundedCornerShape(14.dp)))
                            .padding(horizontal = 11.dp, vertical = 9.dp)
                    ) {
                        Text(msg.text, color = if (mine) Color_White else t.ink, fontSize = 12.sp, lineHeight = 17.sp)
                    }
                    if (!mine && msg.sources != null) {
                        Text(
                            msg.sources,
                            color = t.muted,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 3.dp),
                        )
                    }
                }
            }
            if (thinking) {
                item {
                    Text("Thinking…", color = t.muted, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
                }
            }
        }

        if (quickChips.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(vertical = 6.dp),
            ) {
                items(quickChips) { chip ->
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(t.fieldBg)
                            .border(1.dp, t.line, RoundedCornerShape(99.dp))
                            .clickable { onSend(chip) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(chip, color = t.dim, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(13.dp))
                    .background(t.fieldBg)
                    .border(1.dp, t.line, RoundedCornerShape(13.dp))
                    .padding(horizontal = 12.dp, vertical = 11.dp)
            ) {
                if (draft.isEmpty()) {
                    Text(placeholder, color = t.muted, fontSize = 13.sp)
                }
                BasicTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    textStyle = TextStyle(color = t.ink, fontSize = 13.sp),
                    cursorBrush = SolidColor(t.accent),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Box(
                Modifier
                    .clip(RoundedCornerShape(13.dp))
                    .background(t.accent)
                    .clickable { send() }
                    .padding(horizontal = 14.dp, vertical = 11.dp)
            ) {
                Text("➤", color = Color_White, fontSize = 13.sp)
            }
        }
    }
}

private val Color_White = androidx.compose.ui.graphics.Color.White
