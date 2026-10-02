package com.kotlakiran.toolkit.dataimport

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

/** SAF picker for a single PDF. Returns a launch lambda; result goes to onPicked. */
@Composable
fun rememberPdfPicker(onPicked: (Uri) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) onPicked(uri)
    }
    return { launcher.launch(arrayOf("application/pdf")) }
}

/** SAF picker for CSV/text exports. */
@Composable
fun rememberCsvPicker(onPicked: (Uri) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) onPicked(uri)
    }
    return { launcher.launch(arrayOf("text/*", "text/comma-separated-values", "application/csv")) }
}
