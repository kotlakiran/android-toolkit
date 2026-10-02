package com.kotlakiran.toolkit.storeassets

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.ui.graphics.toArgb
import com.kotlakiran.toolkit.coreui.theme.AppTokens
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Generates Play store assets on-device: 1024x500 feature graphic and
 * 1080x1920 device-framed marketing screenshots, styled from the app's own
 * theme tokens so store art matches the app. Deliberately programmatic —
 * Play policies require screenshots to depict the real app.
 */
class StoreAssetGenerator(private val tokens: AppTokens, private val brand: StoreBrand) {

    private val json = Json { prettyPrint = true }

    /** Everything needed for a Play upload, into outDir. */
    fun generateAll(outDir: File, screens: List<ScreenshotSpec>, listing: StoreListing) {
        outDir.mkdirs()
        File(outDir, "feature-graphic-1024x500.png").writeBitmap(featureGraphic())
        screens.forEachIndexed { i, spec ->
            val file = File(outDir, "screenshot-${i + 1}-${spec.screenName.slug()}.png")
            file.writeBitmap(frameScreenshot(spec))
        }
        File(outDir, "listing.json").writeText(json.encodeToString(StoreListing.serializer(), listing))
    }

    fun featureGraphic(): Bitmap {
        val bmp = Bitmap.createBitmap(FG_WIDTH, FG_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val (c1, c2) = brand.gradient ?: (tokens.accent to tokens.accent2)
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, 0f, FG_WIDTH.toFloat(), FG_HEIGHT.toFloat(), c1.toArgb(), c2.toArgb(), Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, FG_WIDTH.toFloat(), FG_HEIGHT.toFloat(), bg)
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 84f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }
        val tagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(220, 255, 255, 255)
            textSize = 38f
        }
        canvas.drawText(brand.appName, 72f, 238f, titlePaint)
        canvas.drawText(brand.tagline, 72f, 310f, tagPaint)
        return bmp
    }

    fun frameScreenshot(spec: ScreenshotSpec): Bitmap {
        val bmp = Bitmap.createBitmap(SHOT_WIDTH, SHOT_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val (c1, c2) = brand.gradient ?: (tokens.card2 to tokens.bg)
        canvas.drawRect(
            0f, 0f, SHOT_WIDTH.toFloat(), SHOT_HEIGHT.toFloat(),
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(0f, 0f, 0f, SHOT_HEIGHT.toFloat(), c1.toArgb(), c2.toArgb(), Shader.TileMode.CLAMP)
            },
        )

        val headlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = tokens.ink.toArgb()
            textSize = 62f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val captionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = tokens.muted.toArgb()
            textSize = 34f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(spec.headline, SHOT_WIDTH / 2f, 170f, headlinePaint)
        canvas.drawText(spec.caption, SHOT_WIDTH / 2f, 224f, captionPaint)

        // Device frame: rounded bezel, screen scaled to fit inside.
        val frame = RectF(90f, 300f, SHOT_WIDTH - 90f, SHOT_HEIGHT - 110f)
        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = tokens.card.toArgb() }
        canvas.drawRoundRect(frame, 44f, 44f, framePaint)
        val inset = 14f
        val screenRect = RectF(frame.left + inset, frame.top + inset, frame.right - inset, frame.bottom - inset)
        val scale = minOf(screenRect.width() / spec.screen.width, screenRect.height() / spec.screen.height)
        val sw = spec.screen.width * scale
        val sh = spec.screen.height * scale
        val dst = RectF(
            screenRect.centerX() - sw / 2f,
            screenRect.centerY() - sh / 2f,
            screenRect.centerX() + sw / 2f,
            screenRect.centerY() + sh / 2f,
        )
        canvas.drawBitmap(spec.screen, null, dst, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        return bmp
    }

    private fun File.writeBitmap(bitmap: Bitmap) {
        outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun String.slug(): String =
        lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')

    private companion object {
        const val FG_WIDTH = 1024
        const val FG_HEIGHT = 500
        const val SHOT_WIDTH = 1080
        const val SHOT_HEIGHT = 1920
    }
}
