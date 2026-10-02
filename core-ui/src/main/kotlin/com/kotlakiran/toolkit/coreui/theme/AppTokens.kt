package com.kotlakiran.toolkit.coreui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Semantic color tokens shared by all toolkit components and consuming apps. */
data class AppTokens(
    val bg: Color,
    val card: Color,
    val card2: Color,
    val fieldBg: Color,
    val line: Color,
    val ink: Color,
    val muted: Color,
    val dim: Color,
    val accent: Color,
    val accent2: Color,
    val good: Color,
    val warn: Color,
    val bad: Color,
    val isLight: Boolean,
)

val DarkTokens = AppTokens(
    bg = Color(0xFF0F1726),
    card = Color(0xFF141F31),
    card2 = Color(0xFF1B2A44),
    fieldBg = Color(0x0AFFFFFF),
    line = Color(0x12FFFFFF),
    ink = Color(0xFFEAF1FF),
    muted = Color(0xFF8A9BBA),
    dim = Color(0xFF7F93B8),
    accent = Color(0xFF4F7CFF),
    accent2 = Color(0xFF7A63FF),
    good = Color(0xFF5FE0AD),
    warn = Color(0xFFF7B955),
    bad = Color(0xFFFF8A96),
    isLight = false,
)

val LightTokens = AppTokens(
    bg = Color(0xFFF6F8FC),
    card = Color(0xFFFFFFFF),
    card2 = Color(0xFFEFF3FA),
    fieldBg = Color(0xFFF0F3F9),
    line = Color(0x140B1B33),
    ink = Color(0xFF14213A),
    muted = Color(0xFF5B6472),
    dim = Color(0xFF8A93A6),
    accent = Color(0xFF3B63E0),
    accent2 = Color(0xFF6A4FE0),
    good = Color(0xFF1F9E6E),
    warn = Color(0xFFB27409),
    bad = Color(0xFFD8404F),
    isLight = true,
)

val LocalAppTokens = staticCompositionLocalOf { DarkTokens }

@Composable
fun ProvideAppTokens(tokens: AppTokens, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAppTokens provides tokens, content = content)
}
