package com.kotlakiran.toolkit.storeassets

import androidx.compose.ui.graphics.Color
import com.kotlakiran.toolkit.coreui.theme.AppTokens
import kotlinx.serialization.Serializable

/** App branding for generated store assets. */
data class StoreBrand(
    val appName: String,
    val tagline: String,
    /** Brand gradient colors; defaults to the theme's accent pair. */
    val gradient: Pair<Color, Color>? = null,
)

/** One marketing screenshot: the app screen bitmap plus the copy shown above/below it. */
data class ScreenshotSpec(
    val screenName: String,
    val headline: String,
    val caption: String,
    val screen: android.graphics.Bitmap,
)

/** Play Console listing text; written to store listing.json for automation. */
@Serializable
data class StoreListing(
    val title: String,
    val shortDescription: String,
    val fullDescription: String,
    val keywords: List<String> = emptyList(),
)
