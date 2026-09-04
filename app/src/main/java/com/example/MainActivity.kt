package com.example

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.ui.NovaApp
import com.example.ui.NovaNavDestination
import com.example.ui.NovaViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  private val viewModel: NovaViewModel by viewModels()

  private var activeDestination by mutableStateOf(NovaNavDestination.OVERVIEW)
  private var openQrOnLaunch by mutableStateOf(false)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    handleIntent(intent)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        NovaApp(
          viewModel = viewModel,
          initialDestination = activeDestination,
          openQrOnLaunch = openQrOnLaunch
        )
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleIntent(intent)
  }

  private fun handleIntent(intent: Intent?) {
    if (intent == null) return

    when (intent.action) {
      "com.example.ACTION_QUICK_SEND" -> {
        activeDestination = NovaNavDestination.SEND
        openQrOnLaunch = false
      }
      "com.example.ACTION_SCAN_QR" -> {
        activeDestination = NovaNavDestination.OVERVIEW
        openQrOnLaunch = true
      }
      Intent.ACTION_SEND -> {
        activeDestination = NovaNavDestination.SEND
        openQrOnLaunch = false
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
          @Suppress("DEPRECATION")
          intent.getParcelableExtra(Intent.EXTRA_STREAM)
        }

        if (uri != null) {
          processIncomingUri(uri)
        } else {
          val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
          if (!sharedText.isNullOrBlank()) {
            viewModel.addCustomFile(
              name = "Quick_Note_${System.currentTimeMillis() % 10000}.txt",
              sizeBytes = sharedText.toByteArray().size.toLong(),
              type = "DOCUMENT"
            )
          }
        }
      }
      Intent.ACTION_SEND_MULTIPLE -> {
        activeDestination = NovaNavDestination.SEND
        openQrOnLaunch = false
        val uris = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
          @Suppress("DEPRECATION")
          intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
        }
        uris?.forEach { uri ->
          processIncomingUri(uri)
        }
      }
    }
  }

  private fun processIncomingUri(uri: Uri) {
    try {
      var name = "Shared_File_${System.currentTimeMillis() % 10000}"
      var size = 1024L * 1024L
      val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"

      contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
          val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
          val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
          if (nameIndex != -1) name = cursor.getString(nameIndex) ?: name
          if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) size = cursor.getLong(sizeIndex)
        }
      }

      val type = when {
        mimeType.startsWith("image/") -> "IMAGE"
        mimeType.startsWith("video/") -> "VIDEO"
        mimeType.startsWith("audio/") -> "AUDIO"
        mimeType.startsWith("text/") || mimeType.contains("pdf") || mimeType.contains("document") -> "DOCUMENT"
        mimeType.contains("zip") || mimeType.contains("tar") || mimeType.contains("archive") -> "ARCHIVE"
        else -> "FILE"
      }

      viewModel.addCustomFile(
        name = name,
        sizeBytes = size,
        type = type
      )
    } catch (e: Exception) {
      viewModel.addCustomFile(
        name = "Shared_Item_${System.currentTimeMillis() % 10000}.bin",
        sizeBytes = 1024L * 512L,
        type = "FILE"
      )
    }
  }
}
