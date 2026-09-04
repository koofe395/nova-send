package com.example.ui.components

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.SelectedFileItem
import com.example.ui.theme.NovaBackground
import com.example.ui.theme.NovaBorderSubtle
import com.example.ui.theme.NovaPrimary
import com.example.ui.theme.NovaPrimaryLight
import com.example.ui.theme.NovaSuccess
import com.example.ui.theme.NovaSurfaceVariant
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Represents a device file or folder in the File System Explorer.
 */
data class ExplorerFileEntry(
  val id: String,
  val name: String,
  val path: String,
  val sizeBytes: Long,
  val type: String, // DOCUMENT, IMAGE, VIDEO, AUDIO, ARCHIVE, CODE, FOLDER
  val extension: String,
  val lastModified: Long,
  val isDirectory: Boolean = false,
  val uri: Uri? = null
)

/**
 * File System Explorer Component that integrates both in-app directory browsing
 * and standard Android File Provider intents (SAF ACTION_OPEN_DOCUMENT and Photo Picker).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileSystemExplorerDialog(
  onDismiss: () -> Unit,
  onFilesSelected: (List<SelectedFileItem>) -> Unit
) {
  val context = LocalContext.current
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("ALL") }
  var currentPath by remember { mutableStateOf("/storage/emulated/0") }
  var selectedFileMap by remember { mutableStateOf<Map<String, ExplorerFileEntry>>(emptyMap()) }
  var sortBy by remember { mutableStateOf("NAME") } // NAME, SIZE, DATE

  // 1. Android Standard File Provider: Open Multiple Documents (SAF Intent)
  val openDocumentLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenMultipleDocuments()
  ) { uris: List<Uri> ->
    if (uris.isNotEmpty()) {
      val importedItems = uris.mapNotNull { uri ->
        extractFileItemFromUri(context, uri)
      }
      if (importedItems.isNotEmpty()) {
        onFilesSelected(importedItems)
        onDismiss()
      }
    }
  }

  // 2. Android Standard Photo & Video Picker
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20)
  ) { uris: List<Uri> ->
    if (uris.isNotEmpty()) {
      val importedItems = uris.mapNotNull { uri ->
        extractFileItemFromUri(context, uri)
      }
      if (importedItems.isNotEmpty()) {
        onFilesSelected(importedItems)
        onDismiss()
      }
    }
  }

  // Populate realistic device file system data
  val allDeviceFiles = remember { generateDeviceFileSystemEntries(context) }

  val filteredFiles = remember(allDeviceFiles, selectedCategory, searchQuery, sortBy) {
    var list = allDeviceFiles.filter { entry ->
      val matchesCategory = when (selectedCategory) {
        "DOCUMENTS" -> entry.type == "DOCUMENT"
        "DOWNLOADS" -> entry.path.contains("Download", ignoreCase = true)
        "IMAGES" -> entry.type == "IMAGE"
        "VIDEOS" -> entry.type == "VIDEO"
        "AUDIO" -> entry.type == "AUDIO"
        "ARCHIVES" -> entry.type == "ARCHIVE"
        "CODE" -> entry.type == "CODE"
        else -> true
      }
      val matchesQuery = searchQuery.isBlank() || entry.name.contains(searchQuery, ignoreCase = true)
      matchesCategory && matchesQuery
    }

    list = when (sortBy) {
      "SIZE" -> list.sortedByDescending { it.sizeBytes }
      "DATE" -> list.sortedByDescending { it.lastModified }
      else -> list.sortedBy { it.name.lowercase(Locale.ROOT) }
    }
    list
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .background(NovaBackground)
        .testTag("file_system_explorer_screen"),
      color = NovaBackground
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Surface(
          color = Color.White,
          border = BorderStroke(1.dp, NovaBorderSubtle),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("explorer_close_btn")) {
                  Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NovaTextPrimary)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                  Text(
                    text = "File System Explorer",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = NovaTextPrimary
                  )
                  Text(
                    text = "Device Storage & Android File Provider",
                    fontSize = 11.sp,
                    color = NovaTextSecondary
                  )
                }
              }

              // Standard Intent Buttons
              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Photo Picker Quick Action
                IconButton(
                  onClick = {
                    photoPickerLauncher.launch(
                      PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                    )
                  },
                  modifier = Modifier.testTag("explorer_photo_picker_btn")
                ) {
                  Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = "Android Photo Picker",
                    tint = NovaPrimary
                  )
                }

                // Standard SAF Document Provider Intent Button
                Button(
                  onClick = {
                    // Launches standard Android File Provider intent
                    openDocumentLauncher.launch(arrayOf("*/*"))
                  },
                  shape = RoundedCornerShape(100.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = NovaPrimary),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                  modifier = Modifier.testTag("explorer_saf_intent_btn")
                ) {
                  Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("System Picker", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar & Sort
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                  .weight(1f)
                  .testTag("explorer_search_input"),
                placeholder = { Text("Search files, documents, media...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NovaTextMuted, modifier = Modifier.size(18.dp)) },
                shape = RoundedCornerShape(100.dp),
                colors = TextFieldDefaults.colors(
                  focusedContainerColor = NovaSurfaceVariant,
                  unfocusedContainerColor = NovaSurfaceVariant,
                  focusedIndicatorColor = NovaPrimary,
                  unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
              )

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(100.dp))
                  .background(NovaSurfaceVariant)
                  .clickable {
                    sortBy = when (sortBy) {
                      "NAME" -> "SIZE"
                      "SIZE" -> "DATE"
                      else -> "NAME"
                    }
                  }
                  .padding(horizontal = 12.dp, vertical = 10.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Sort, contentDescription = null, tint = NovaPrimary, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = when (sortBy) {
                      "SIZE" -> "Size"
                      "DATE" -> "Date"
                      else -> "Name"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NovaTextPrimary
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Storage Category Pills
            LazyRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              item { CategoryPill("All Files", "ALL", selectedCategory) { selectedCategory = it } }
              item { CategoryPill("Downloads", "DOWNLOADS", selectedCategory) { selectedCategory = it } }
              item { CategoryPill("Documents", "DOCUMENTS", selectedCategory) { selectedCategory = it } }
              item { CategoryPill("Photos", "IMAGES", selectedCategory) { selectedCategory = it } }
              item { CategoryPill("Videos", "VIDEOS", selectedCategory) { selectedCategory = it } }
              item { CategoryPill("Audio", "AUDIO", selectedCategory) { selectedCategory = it } }
              item { CategoryPill("Archives", "ARCHIVES", selectedCategory) { selectedCategory = it } }
              item { CategoryPill("Code", "CODE", selectedCategory) { selectedCategory = it } }
            }
          }
        }

        // Breadcrumbs & Storage Info
        Surface(
          color = NovaSurfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.FolderOpen, contentDescription = null, tint = NovaPrimary, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Internal Storage > ${selectedCategory.lowercase().replaceFirstChar { it.uppercase() }}",
                fontSize = 11.sp,
                color = NovaTextSecondary,
                fontWeight = FontWeight.Medium
              )
            }

            // Select All / Deselect All
            Text(
              text = if (selectedFileMap.size == filteredFiles.size && filteredFiles.isNotEmpty()) "Deselect All" else "Select All",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = NovaPrimary,
              modifier = Modifier
                .clickable {
                  if (selectedFileMap.size == filteredFiles.size) {
                    selectedFileMap = emptyMap()
                  } else {
                    selectedFileMap = filteredFiles.associateBy { it.id }
                  }
                }
                .testTag("explorer_toggle_select_all")
            )
          }
        }

        // Files List
        LazyColumn(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          if (filteredFiles.isEmpty()) {
            item {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(40.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "No files found in this category.",
                  fontSize = 13.sp,
                  color = NovaTextMuted
                )
              }
            }
          } else {
            items(filteredFiles, key = { it.id }) { file ->
              val isSelected = selectedFileMap.containsKey(file.id)

              ExplorerFileCard(
                file = file,
                isSelected = isSelected,
                onToggle = {
                  selectedFileMap = if (isSelected) {
                    selectedFileMap - file.id
                  } else {
                    selectedFileMap + (file.id to file)
                  }
                }
              )
            }
          }
        }

        // Bottom Selection Bar
        Surface(
          color = Color.White,
          border = BorderStroke(1.dp, NovaBorderSubtle),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            val totalSelectedBytes = selectedFileMap.values.sumOf { it.sizeBytes }

            Column {
              Text(
                text = "${selectedFileMap.size} item(s) selected",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NovaTextPrimary
              )
              Text(
                text = formatBytes(totalSelectedBytes),
                fontSize = 11.sp,
                color = NovaPrimary,
                fontWeight = FontWeight.Medium
              )
            }

            Button(
              onClick = {
                val chosenItems = selectedFileMap.values.map { entry ->
                  SelectedFileItem(
                    id = entry.id,
                    name = entry.name,
                    sizeBytes = entry.sizeBytes,
                    type = entry.type,
                    extension = entry.extension,
                    isCustom = true
                  )
                }
                onFilesSelected(chosenItems)
                onDismiss()
              },
              enabled = selectedFileMap.isNotEmpty(),
              shape = RoundedCornerShape(100.dp),
              colors = ButtonDefaults.buttonColors(containerColor = NovaPrimary),
              modifier = Modifier.testTag("explorer_confirm_selection_btn")
            ) {
              Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Add to Transfer", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CategoryPill(
  label: String,
  key: String,
  selectedKey: String,
  onSelect: (String) -> Unit
) {
  val isSelected = key == selectedKey
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(100.dp))
      .background(if (isSelected) NovaPrimary else NovaSurfaceVariant)
      .border(1.dp, if (isSelected) NovaPrimary else NovaBorderSubtle, RoundedCornerShape(100.dp))
      .clickable { onSelect(key) }
      .padding(horizontal = 12.dp, vertical = 5.dp)
  ) {
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
      color = if (isSelected) Color.White else NovaTextSecondary
    )
  }
}

@Composable
private fun ExplorerFileCard(
  file: ExplorerFileEntry,
  isSelected: Boolean,
  onToggle: () -> Unit
) {
  val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
  val dateString = remember(file.lastModified) { dateFormat.format(Date(file.lastModified)) }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onToggle() }
      .testTag("explorer_file_${file.id}"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) NovaPrimaryLight else Color.White
    ),
    border = BorderStroke(
      if (isSelected) 1.5.dp else 1.dp,
      if (isSelected) NovaPrimary else NovaBorderSubtle
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(getFileTypeColor(file.type).copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = getFileTypeIcon(file.type),
            contentDescription = null,
            tint = getFileTypeColor(file.type),
            modifier = Modifier.size(22.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = file.name,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = NovaTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = "${formatBytes(file.sizeBytes)} • $dateString • .${file.extension.uppercase()}",
            fontSize = 11.sp,
            color = NovaTextSecondary
          )
        }
      }

      Checkbox(
        checked = isSelected,
        onCheckedChange = { onToggle() },
        colors = CheckboxDefaults.colors(
          checkedColor = NovaPrimary,
          checkmarkColor = Color.White
        ),
        modifier = Modifier.testTag("explorer_checkbox_${file.id}")
      )
    }
  }
}

/**
 * Helper to query Android ContentResolver and extract file metadata from a content URI.
 */
fun extractFileItemFromUri(context: Context, uri: Uri): SelectedFileItem? {
  return try {
    var displayName = "Selected_File"
    var sizeBytes = 0L

    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
      val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
      val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
      if (cursor.moveToFirst()) {
        if (nameIndex != -1) {
          displayName = cursor.getString(nameIndex) ?: displayName
        }
        if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
          sizeBytes = cursor.getLong(sizeIndex)
        }
      }
    }

    val extension = displayName.substringAfterLast(".", "").lowercase()
    val type = when (extension) {
      "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv" -> "DOCUMENT"
      "jpg", "jpeg", "png", "webp", "gif", "svg", "heic" -> "IMAGE"
      "mp4", "mkv", "mov", "avi", "webm" -> "VIDEO"
      "mp3", "wav", "flac", "aac", "m4a", "ogg" -> "AUDIO"
      "zip", "tar", "gz", "7z", "rar" -> "ARCHIVE"
      "kt", "java", "json", "xml", "html", "js", "ts", "py" -> "CODE"
      else -> "FILE"
    }

    SelectedFileItem(
      id = UUID.randomUUID().toString(),
      name = displayName,
      sizeBytes = if (sizeBytes > 0) sizeBytes else 1_500_000L,
      type = type,
      extension = extension,
      isCustom = true
    )
  } catch (e: Exception) {
    null
  }
}

/**
 * Generates sample and realistic device storage entries across multiple standard folders.
 */
private fun generateDeviceFileSystemEntries(context: Context): List<ExplorerFileEntry> {
  val now = System.currentTimeMillis()
  val oneDay = 86_400_000L

  val entries = mutableListOf(
    ExplorerFileEntry(
      id = "doc_1",
      name = "Quarterly_Financial_Report_2026.pdf",
      path = "/storage/emulated/0/Documents/Quarterly_Financial_Report_2026.pdf",
      sizeBytes = 14_800_000L,
      type = "DOCUMENT",
      extension = "pdf",
      lastModified = now - oneDay * 2
    ),
    ExplorerFileEntry(
      id = "doc_2",
      name = "Project_Nova_Architecture_v3.docx",
      path = "/storage/emulated/0/Documents/Project_Nova_Architecture_v3.docx",
      sizeBytes = 3_450_000L,
      type = "DOCUMENT",
      extension = "docx",
      lastModified = now - oneDay * 5
    ),
    ExplorerFileEntry(
      id = "dl_1",
      name = "Ubuntu_24_04_LTS_Desktop.iso",
      path = "/storage/emulated/0/Download/Ubuntu_24_04_LTS_Desktop.iso",
      sizeBytes = 4_800_000_000L,
      type = "ARCHIVE",
      extension = "iso",
      lastModified = now - oneDay * 1
    ),
    ExplorerFileEntry(
      id = "dl_2",
      name = "Client_Assets_Master_Bundle.zip",
      path = "/storage/emulated/0/Download/Client_Assets_Master_Bundle.zip",
      sizeBytes = 385_000_000L,
      type = "ARCHIVE",
      extension = "zip",
      lastModified = now - oneDay * 3
    ),
    ExplorerFileEntry(
      id = "img_1",
      name = "Product_Launch_Banner_4K.png",
      path = "/storage/emulated/0/DCIM/Camera/Product_Launch_Banner_4K.png",
      sizeBytes = 8_200_000L,
      type = "IMAGE",
      extension = "png",
      lastModified = now - oneDay * 4
    ),
    ExplorerFileEntry(
      id = "img_2",
      name = "Tokyo_Night_Street_RAW.dng",
      path = "/storage/emulated/0/DCIM/Camera/Tokyo_Night_Street_RAW.dng",
      sizeBytes = 42_000_000L,
      type = "IMAGE",
      extension = "dng",
      lastModified = now - oneDay * 8
    ),
    ExplorerFileEntry(
      id = "vid_1",
      name = "Cinematic_Hero_Reel_1080p60.mp4",
      path = "/storage/emulated/0/Movies/Cinematic_Hero_Reel_1080p60.mp4",
      sizeBytes = 680_000_000L,
      type = "VIDEO",
      extension = "mp4",
      lastModified = now - oneDay * 2
    ),
    ExplorerFileEntry(
      id = "vid_2",
      name = "Keynote_Presentation_Recording.mov",
      path = "/storage/emulated/0/Movies/Keynote_Presentation_Recording.mov",
      sizeBytes = 1_250_000_000L,
      type = "VIDEO",
      extension = "mov",
      lastModified = now - oneDay * 6
    ),
    ExplorerFileEntry(
      id = "aud_1",
      name = "Master_Audio_Soundtrack_24bit.flac",
      path = "/storage/emulated/0/Music/Master_Audio_Soundtrack_24bit.flac",
      sizeBytes = 88_000_000L,
      type = "AUDIO",
      extension = "flac",
      lastModified = now - oneDay * 7
    ),
    ExplorerFileEntry(
      id = "aud_2",
      name = "Team_Debrief_Podcast_Episode_12.mp3",
      path = "/storage/emulated/0/Music/Team_Debrief_Podcast_Episode_12.mp3",
      sizeBytes = 32_000_000L,
      type = "AUDIO",
      extension = "mp3",
      lastModified = now - oneDay * 10
    ),
    ExplorerFileEntry(
      id = "code_1",
      name = "mesh_routing_algorithm.kt",
      path = "/storage/emulated/0/Documents/mesh_routing_algorithm.kt",
      sizeBytes = 45_000L,
      type = "CODE",
      extension = "kt",
      lastModified = now - oneDay * 1
    ),
    ExplorerFileEntry(
      id = "code_2",
      name = "network_security_rules.json",
      path = "/storage/emulated/0/Documents/network_security_rules.json",
      sizeBytes = 18_000L,
      type = "CODE",
      extension = "json",
      lastModified = now - oneDay * 3
    )
  )

  // Also query actual files if existing in app's internal or external directories
  try {
    val externalDirs = context.getExternalFilesDirs(null)
    externalDirs.forEach { dir ->
      dir?.listFiles()?.forEach { file ->
        if (file.isFile) {
          val ext = file.name.substringAfterLast(".", "bin")
          entries.add(
            ExplorerFileEntry(
              id = file.absolutePath,
              name = file.name,
              path = file.absolutePath,
              sizeBytes = file.length(),
              type = when (ext.lowercase()) {
                "pdf", "doc", "docx", "txt" -> "DOCUMENT"
                "jpg", "png", "webp" -> "IMAGE"
                "mp4", "mkv" -> "VIDEO"
                else -> "FILE"
              },
              extension = ext,
              lastModified = file.lastModified()
            )
          )
        }
      }
    }
  } catch (e: Exception) {
    // Ignore safe fallback
  }

  return entries
}
