package com.kotlakiran.toolkit.dataimport

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.encryption.InvalidPasswordException
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/** Extracts text from bank statement PDFs entirely on-device; raw files are never uploaded. */
class PdfStatementImporter(private val context: Context) {

    class WrongPasswordException : IOException("PDF password is wrong")
    class NotAPdfException : IOException("Selected file is not a readable PDF")

    @Volatile
    private var initialized = false

    private fun ensureInit() {
        if (!initialized) {
            PDFBoxResourceLoader.init(context.applicationContext)
            initialized = true
        }
    }

    suspend fun extractText(uri: Uri, password: String? = null): String = withContext(Dispatchers.IO) {
        ensureInit()
        val input = context.contentResolver.openInputStream(uri) ?: throw NotAPdfException()
        input.use { stream ->
            try {
                PDDocument.load(stream, password ?: "").use { doc ->
                    PDFTextStripper().getText(doc)
                }
            } catch (e: InvalidPasswordException) {
                throw WrongPasswordException()
            }
        }
    }

    suspend fun import(
        uri: Uri,
        password: String? = null,
        parsers: List<StatementParser> = StatementParsers.defaults,
    ): ParsedStatement = StatementParsers.parseBest(extractText(uri, password), parsers)
}
