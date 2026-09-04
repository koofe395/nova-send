package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ContactEntity
import com.example.data.model.SelectedFileItem
import com.example.engine.EngineState
import com.example.ui.NovaViewModel
import com.example.ui.components.FileSystemExplorerDialog
import com.example.ui.components.NearDevicePermissionBanner
import com.example.ui.components.StatusBadge
import com.example.ui.components.UserAvatar
import com.example.ui.components.extractFileItemFromUri
import com.example.ui.components.formatBytes
import com.example.ui.components.getFileTypeColor
import com.example.ui.components.getFileTypeIcon
import com.example.ui.components.rememberNearDevicePermissionState
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaError
import com.example.ui.theme.NovaPrimary
import com.example.ui.theme.NovaPrimaryLight
import com.example.ui.theme.NovaSuccess
import com.example.ui.theme.NovaSuccessBg
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.theme.NovaWarning

@Composable
fun SendScreen(
  viewModel: NovaViewModel,
  onFinishFlow: () -> Unit,
  modifier: Modifier = Modifier
) {
  val step by viewModel.sendStep.collectAsStateWithLifecycle()
  val selectedFiles by viewModel.selectedFiles.collectAsStateWithLifecycle()
  val selectedRecipient by viewModel.selectedRecipient.collectAsStateWithLifecycle()
  val contacts by viewModel.allContacts.collectAsStateWithLifecycle()
  val activeProgress by viewModel.activeProgress.collectAsStateWithLifecycle()

  var showAddFileDialog by remember { mutableStateOf(false) }
  var showExplorerDialog by remember { mutableStateOf(false) }
  var showSearchingDevicesScreen by remember { mutableStateOf(false) }

  // If the user triggered the dedicated Searching for Devices screen
  if (showSearchingDevicesScreen) {
    SearchingDevicesScreen(
      onDeviceSelected = { scanned ->
        val contact = ContactEntity(
          id = scanned.id,
          name = scanned.name,
          username = scanned.owner.lowercase().replace(" ", ""),
          deviceName = scanned.name,
          isOnline = true,
          isVerified = scanned.isTrusted,
          isFavorite = false,
          avatarInitials = scanned.avatarInitials,
          avatarColorHex = scanned.avatarColorHex
        )
        viewModel.selectRecipient(contact)
        showSearchingDevicesScreen = false
        viewModel.setSendStep(3)
      },
      onBack = { showSearchingDevicesScreen = false }
    )
    return
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(com.example.ui.theme.NovaBackground)
      .testTag("send_screen")
  ) {
    // 1. Step Indicator Bar
    StepProgressHeader(
      currentStep = step,
      onStepClick = { targetStep ->
        if (targetStep < step && step != 4) {
          viewModel.setSendStep(targetStep)
        }
      }
    )

    // 2. Step Views
    Box(modifier = Modifier.weight(1f)) {
      when (step) {
        1 -> Step1SelectContent(
          selectedFiles = selectedFiles,
          onAddFile = { viewModel.selectFile(it) },
          onRemoveFile = { viewModel.removeFile(it) },
          onOpenExplorer = { showExplorerDialog = true },
          onOpenCustomDialog = { showAddFileDialog = true },
          onNext = { viewModel.setSendStep(2) }
        )
        2 -> Step2SelectRecipient(
          contacts = contacts,
          selectedRecipient = selectedRecipient,
          onSelect = { viewModel.selectRecipient(it) },
          onOpenRadarScan = { showSearchingDevicesScreen = true },
          onBack = { viewModel.setSendStep(1) },
          onNext = { viewModel.setSendStep(3) }
        )
        3 -> Step3Verification(
          selectedFiles = selectedFiles,
          recipient = selectedRecipient,
          onBack = { viewModel.setSendStep(2) },
          onConfirmSend = { viewModel.startSending() }
        )
        4 -> Step4TransferMonitor(
          progress = activeProgress,
          onPauseToggle = { viewModel.togglePauseTransfer() },
          onCancel = { viewModel.cancelActiveTransfer() },
          onSendAnother = { viewModel.resetSendFlow() },
          onDone = onFinishFlow
        )
      }
    }
  }

  // File System Explorer Component
  if (showExplorerDialog) {
    FileSystemExplorerDialog(
      onDismiss = { showExplorerDialog = false },
      onFilesSelected = { files ->
        files.forEach { viewModel.selectFile(it) }
        showExplorerDialog = false
      }
    )
  }

  // Quick Add Custom File Dialog
  if (showAddFileDialog) {
    AddCustomFileDialog(
      onDismiss = { showAddFileDialog = false },
      onAdd = { name, sizeBytes, type ->
        viewModel.addCustomFile(name, sizeBytes, type)
        showAddFileDialog = false
      }
    )
  }
}

@Composable
private fun StepProgressHeader(
  currentStep: Int,
  onStepClick: (Int) -> Unit
) {
  Surface(
    color = Color.White,
    border = BorderStroke(1.dp, NovaBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = when (currentStep) {
            1 -> "STEP 1 — SELECT CONTENT"
            2 -> "STEP 2 — SELECT RECIPIENT"
            3 -> "STEP 3 — VERIFICATION & SECURITY"
            else -> "STEP 4 — TRANSFER ACTIVE"
          },
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = NovaPrimary
        )

        Text(
          text = "$currentStep of 4",
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          color = NovaTextMuted
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        for (i in 1..4) {
          Box(
            modifier = Modifier
              .weight(1f)
              .height(4.dp)
              .clip(RoundedCornerShape(2.dp))
              .background(
                when {
                  i < currentStep -> NovaSuccess
                  i == currentStep -> NovaPrimary
                  else -> Color(0xFFE2E8F0)
                }
              )
              .clickable(enabled = i < currentStep && currentStep != 4) { onStepClick(i) }
          )
        }
      }
    }
  }
}

// -------------------------------------------------------------
// STEP 1: Select Content
// -------------------------------------------------------------
@Composable
private fun Step1SelectContent(
  selectedFiles: List<SelectedFileItem>,
  onAddFile: (SelectedFileItem) -> Unit,
  onRemoveFile: (String) -> Unit,
  onOpenExplorer: () -> Unit,
  onOpenCustomDialog: () -> Unit,
  onNext: () -> Unit
) {
  val context = LocalContext.current
  val totalBytes = selectedFiles.sumOf { it.sizeBytes }

  // 1. Android Standard SAF Document Provider Intent
  val safDocumentLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenMultipleDocuments()
  ) { uris ->
    uris.forEach { uri ->
      extractFileItemFromUri(context, uri)?.let { onAddFile(it) }
    }
  }

  // 2. Android Photo & Video Picker Intent
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickMultipleVisualMedia()
  ) { uris ->
    uris.forEach { uri ->
      extractFileItemFromUri(context, uri)?.let { onAddFile(it) }
    }
  }

  val presetOptions = listOf(
    SelectedFileItem("preset_1", "Contract_Signed_Final.pdf", 4_200_000L, "DOCUMENT", "pdf"),
    SelectedFileItem("preset_2", "Raw_4K_Render_Edit.mp4", 620_000_000L, "VIDEO", "mp4"),
    SelectedFileItem("preset_3", "Design_System_Icons.zip", 38_500_000L, "ARCHIVE", "zip"),
    SelectedFileItem("preset_4", "App_Architecture_Diagram.png", 3_400_000L, "IMAGE", "png"),
    SelectedFileItem("preset_5", "Master_Audio_Mix.wav", 88_000_000L, "AUDIO", "wav")
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    LazyColumn(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Upload Dropzone Card - Opens File System Explorer
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenExplorer() }
            .testTag("upload_dropzone"),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.5.dp, NovaPrimary.copy(alpha = 0.35f))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(NovaPrimaryLight),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.FolderOpen,
                contentDescription = null,
                tint = NovaPrimary,
                modifier = Modifier.size(28.dp)
              )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "File System Explorer & Storage",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = NovaTextPrimary
            )
            Text(
              text = "Explore internal folders, downloads, documents, and media",
              fontSize = 12.sp,
              color = NovaTextSecondary,
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = onOpenExplorer,
                colors = ButtonDefaults.buttonColors(containerColor = NovaPrimary),
                shape = RoundedCornerShape(100.dp),
                modifier = Modifier.weight(1f).testTag("open_file_explorer_btn")
              ) {
                Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open Explorer", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
              }

              OutlinedButton(
                onClick = { safDocumentLauncher.launch(arrayOf("*/*")) },
                shape = RoundedCornerShape(100.dp),
                border = BorderStroke(1.dp, NovaPrimary.copy(alpha = 0.5f)),
                modifier = Modifier.weight(1f).testTag("open_saf_picker_btn")
              ) {
                Icon(Icons.Default.UploadFile, contentDescription = null, tint = NovaPrimary, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("System Picker", fontSize = 12.sp, color = NovaPrimary, fontWeight = FontWeight.SemiBold)
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              horizontalArrangement = Arrangement.spacedBy(16.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "• Photos & Videos",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = NovaPrimary,
                modifier = Modifier.clickable {
                  photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                  )
                }
              )
              Text(
                text = "• Custom Name Entry",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = NovaTextSecondary,
                modifier = Modifier.clickable { onOpenCustomDialog() }
              )
            }
          }
        }
      }

      // Quick Preset Adders
      item {
        Column {
          Text(
            text = "Quick Select Real File Presets:",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = NovaTextPrimary
          )
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            presetOptions.take(3).forEach { preset ->
              val isAdded = selectedFiles.any { it.name == preset.name }
              OutlinedButton(
                onClick = { if (!isAdded) onAddFile(preset) },
                modifier = Modifier.weight(1f).height(38.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (isAdded) NovaSuccess else NovaBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                  containerColor = if (isAdded) NovaSuccessBg else Color.White
                )
              ) {
                Text(
                  text = if (isAdded) "✓ Added" else preset.extension.uppercase(),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isAdded) NovaSuccess else NovaTextPrimary
                )
              }
            }
          }
        }
      }

      // Selected Items List
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Selected Items (${selectedFiles.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = NovaTextPrimary
          )
          if (selectedFiles.isNotEmpty()) {
            Text(
              text = formatBytes(totalBytes),
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = NovaPrimary
            )
          }
        }
      }

      if (selectedFiles.isEmpty()) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, NovaBorder)
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "No content selected yet. Tap above or pick a preset.",
                fontSize = 13.sp,
                color = NovaTextMuted
              )
            }
          }
        }
      } else {
        items(selectedFiles, key = { it.id }) { file ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, NovaBorder)
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
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(getFileTypeColor(file.type).copy(alpha = 0.12f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = getFileTypeIcon(file.type),
                    contentDescription = null,
                    tint = getFileTypeColor(file.type),
                    modifier = Modifier.size(20.dp)
                  )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = file.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = NovaTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = "${file.type} • ${formatBytes(file.sizeBytes)}",
                    fontSize = 11.sp,
                    color = NovaTextSecondary
                  )
                }
              }

              IconButton(
                onClick = { onRemoveFile(file.id) },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(Icons.Default.Close, contentDescription = "Remove", tint = NovaTextMuted)
              }
            }
          }
        }
      }
    }

    // Step 1 Bottom Action
    Surface(
      color = Color.White,
      border = BorderStroke(1.dp, NovaBorder),
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(text = "Total Payload", fontSize = 11.sp, color = NovaTextMuted)
          Text(
            text = "${selectedFiles.size} items • ${formatBytes(totalBytes)}",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = NovaTextPrimary
          )
        }

        Button(
          onClick = onNext,
          enabled = selectedFiles.isNotEmpty(),
          colors = ButtonDefaults.buttonColors(containerColor = NovaPrimary),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("step1_next_btn")
        ) {
          Text("Select Recipient", fontSize = 13.sp)
          Spacer(modifier = Modifier.width(6.dp))
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
        }
      }
    }
  }
}

// -------------------------------------------------------------
// STEP 2: Select Recipient
// -------------------------------------------------------------
@Composable
private fun Step2SelectRecipient(
  contacts: List<ContactEntity>,
  selectedRecipient: ContactEntity?,
  onSelect: (ContactEntity) -> Unit,
  onOpenRadarScan: () -> Unit,
  onBack: () -> Unit,
  onNext: () -> Unit
) {
  val permissionState = rememberNearDevicePermissionState()
  var searchQuery by remember { mutableStateOf("") }

  val filteredContacts = remember(contacts, searchQuery) {
    if (searchQuery.isBlank()) contacts else contacts.filter {
      it.name.contains(searchQuery, ignoreCase = true) ||
        it.username.contains(searchQuery, ignoreCase = true) ||
        it.deviceName.contains(searchQuery, ignoreCase = true)
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    // Near-Device Permission Status Banner
    NearDevicePermissionBanner(permissionState = permissionState)

    Spacer(modifier = Modifier.height(10.dp))

    // Radar Scan Hero Card (Visual Feedback Scanner)
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .clickable { onOpenRadarScan() }
        .testTag("radar_scan_hero_card"),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = NovaPrimaryLight),
      border = BorderStroke(1.dp, NovaPrimary.copy(alpha = 0.35f))
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(NovaPrimary),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Radar,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(24.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Radar Device Scanner",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = NovaTextPrimary
            )
            Text(
              text = "Animated radar sweep across 5GHz Wi-Fi & BLE",
              fontSize = 11.sp,
              color = NovaTextSecondary
            )
          }
        }

        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = null,
          tint = NovaPrimary,
          modifier = Modifier.size(18.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Search Bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("search_recipient_field"),
      placeholder = { Text("Search nearby devices, contacts, @username...", fontSize = 13.sp) },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NovaTextMuted) },
      shape = RoundedCornerShape(12.dp),
      colors = TextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedIndicatorColor = NovaPrimary,
        unfocusedIndicatorColor = NovaBorder
      ),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(14.dp))

    LazyColumn(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      item {
        Text(
          text = "Available Devices & Contacts",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = NovaTextPrimary
        )
      }

      items(filteredContacts, key = { it.id }) { contact ->
        val isSelected = selectedRecipient?.id == contact.id

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(contact) }
            .testTag("contact_item_${contact.id}"),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isSelected) NovaPrimaryLight else Color.White
          ),
          border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) NovaPrimary else NovaBorder
          )
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              UserAvatar(
                name = contact.name,
                initials = contact.avatarInitials,
                colorHex = contact.avatarColorHex,
                size = 46.dp,
                isOnline = contact.isOnline,
                isVerified = contact.isVerified
              )

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = contact.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NovaTextPrimary
                  )
                  if (contact.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                      imageVector = Icons.Default.Verified,
                      contentDescription = "Verified",
                      tint = NovaPrimary,
                      modifier = Modifier.size(14.dp)
                    )
                  }
                }
                Text(
                  text = "@${contact.username} • ${if (contact.isOnline) "Online" else "Idle"}",
                  fontSize = 12.sp,
                  color = if (contact.isOnline) NovaSuccess else NovaTextMuted
                )
                Text(
                  text = "Device: ${contact.deviceName}",
                  fontSize = 11.sp,
                  color = NovaTextSecondary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }

            Box(
              modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .border(
                  2.dp,
                  if (isSelected) NovaPrimary else Color(0xFFCBD5E1),
                  CircleShape
                )
                .background(if (isSelected) NovaPrimary else Color.Transparent),
              contentAlignment = Alignment.Center
            ) {
              if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
              }
            }
          }
        }
      }
    }

    // Step 2 Bottom Actions
    Surface(
      color = Color.White,
      border = BorderStroke(1.dp, NovaBorder),
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedButton(
          onClick = onBack,
          shape = RoundedCornerShape(10.dp),
          border = BorderStroke(1.dp, NovaBorder)
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Back", fontSize = 13.sp)
        }

        Button(
          onClick = onNext,
          enabled = selectedRecipient != null,
          colors = ButtonDefaults.buttonColors(containerColor = NovaPrimary),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("step2_next_btn")
        ) {
          Text("Verify Recipient", fontSize = 13.sp)
          Spacer(modifier = Modifier.width(6.dp))
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
        }
      }
    }
  }
}

// -------------------------------------------------------------
// STEP 3: Verification & Security Confirmation
// -------------------------------------------------------------
@Composable
private fun Step3Verification(
  selectedFiles: List<SelectedFileItem>,
  recipient: ContactEntity?,
  onBack: () -> Unit,
  onConfirmSend: () -> Unit
) {
  val totalBytes = selectedFiles.sumOf { it.sizeBytes }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    LazyColumn(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        Text(
          text = "Security Handshake Verification",
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
          color = NovaTextPrimary
        )
        Text(
          text = "Confirm target recipient and cryptographic integrity parameters.",
          fontSize = 12.sp,
          color = NovaTextSecondary
        )
      }

      // Transfer Relationship Card (Sender -> P2P Link -> Recipient)
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.dp, NovaBorder)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            // SENDER
            Row(verticalAlignment = Alignment.CenterVertically) {
              UserAvatar(
                name = "Mohamed",
                initials = "MO",
                colorHex = 0xFF4F46E5,
                size = 40.dp,
                isOnline = true
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text("Sender (You)", fontSize = 11.sp, color = NovaTextMuted)
                Text("Mohamed Ahmed (@moha)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NovaTextPrimary)
                Text("Pixel 9 Pro (Active Direct Node)", fontSize = 11.sp, color = NovaTextSecondary)
              }
            }

            // Connection Link Pillar
            Box(
              modifier = Modifier
                .padding(start = 20.dp, top = 4.dp, bottom = 4.dp)
                .width(2.dp)
                .height(28.dp)
                .background(NovaPrimary)
            )

            // RECIPIENT
            recipient?.let { rec ->
              Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(
                  name = rec.name,
                  initials = rec.avatarInitials,
                  colorHex = rec.avatarColorHex,
                  size = 40.dp,
                  isOnline = rec.isOnline,
                  isVerified = rec.isVerified
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text("Recipient", fontSize = 11.sp, color = NovaTextMuted)
                  Text("${rec.name} (@${rec.username})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NovaTextPrimary)
                  Text("Device: ${rec.deviceName}", fontSize = 11.sp, color = NovaTextSecondary)
                }
              }
            }
          }
        }
      }

      // Security Parameters Card
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.dp, NovaBorder)
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
              text = "Security Guarantees",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = NovaTextPrimary
            )

            SecurityCheckRow(title = "Recipient Identity Verified", subtitle = "Public key matched with trusted ledger")
            SecurityCheckRow(title = "End-to-End Encryption", subtitle = "AES-256-GCM session key generated locally")
            SecurityCheckRow(title = "Integrity Hash Pre-Calculated", subtitle = "SHA-256 integrity checksum ready")
            SecurityCheckRow(title = "Direct Peer-to-Peer", subtitle = "No intermediate cloud server storing payload")
          }
        }
      }

      // Summary
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Total Transfer Payload", fontSize = 12.sp, color = NovaTextSecondary)
              Text(
                text = "${selectedFiles.size} Files • ${formatBytes(totalBytes)}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = NovaTextPrimary
              )
            }
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(NovaSuccessBg)
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text("Ready to Send", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NovaSuccess)
            }
          }
        }
      }
    }

    // Step 3 Actions
    Surface(
      color = Color.White,
      border = BorderStroke(1.dp, NovaBorder),
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedButton(
          onClick = onBack,
          shape = RoundedCornerShape(10.dp),
          border = BorderStroke(1.dp, NovaBorder)
        ) {
          Text("Cancel", fontSize = 13.sp)
        }

        Button(
          onClick = onConfirmSend,
          colors = ButtonDefaults.buttonColors(containerColor = NovaPrimary),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("confirm_send_request_btn")
        ) {
          Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Send Request", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
private fun SecurityCheckRow(title: String, subtitle: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(
      imageVector = Icons.Default.CheckCircle,
      contentDescription = null,
      tint = NovaSuccess,
      modifier = Modifier.size(16.dp)
    )
    Spacer(modifier = Modifier.width(10.dp))
    Column {
      Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NovaTextPrimary)
      Text(text = subtitle, fontSize = 11.sp, color = NovaTextSecondary)
    }
  }
}

// -------------------------------------------------------------
// STEP 4: Live Transfer Monitor
// -------------------------------------------------------------
@Composable
private fun Step4TransferMonitor(
  progress: com.example.engine.ActiveTransferProgress,
  onPauseToggle: () -> Unit,
  onCancel: () -> Unit,
  onSendAnother: () -> Unit,
  onDone: () -> Unit
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse_beam")
  val beamPulse by infiniteTransition.animateFloat(
    initialValue = 0.3f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(600),
      repeatMode = RepeatMode.Reverse
    ),
    label = "beam_pulse"
  )

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top State Badge
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Transfer Session",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = NovaTextPrimary
          )
          Text(
            text = progress.statusMessage,
            fontSize = 12.sp,
            color = NovaPrimary,
            fontWeight = FontWeight.Medium
          )
        }
        StatusBadge(status = progress.state.name)
      }
    }

    // Sender -> Receiver Visual Link
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, NovaBorder)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          // Sender
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            UserAvatar(
              name = "You",
              initials = "MO",
              colorHex = 0xFF4F46E5,
              size = 48.dp,
              isOnline = true
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text("You (Sender)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NovaTextPrimary)
            Text("Pixel 9 Pro", fontSize = 10.sp, color = NovaTextMuted)
          }

          // Animated Stream Beam
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
          ) {
            Text(
              text = if (progress.speedMbPerSec > 0) "${"%.1f".format(progress.speedMbPerSec)} MB/s" else "Connecting",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = NovaPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(NovaPrimary.copy(alpha = beamPulse))
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (progress.etaSeconds > 0) "${progress.etaSeconds}s remaining" else "P2P Stream",
              fontSize = 10.sp,
              color = NovaTextMuted
            )
          }

          // Receiver
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            UserAvatar(
              name = progress.partnerName.ifBlank { "Recipient" },
              initials = progress.partnerName.take(2).ifBlank { "RC" },
              colorHex = 0xFF6366F1,
              size = 48.dp,
              isOnline = true,
              isVerified = true
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(progress.partnerName.ifBlank { "Recipient" }, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NovaTextPrimary)
            Text(progress.partnerDevice.ifBlank { "Device" }, fontSize = 10.sp, color = NovaTextMuted)
          }
        }
      }
    }

    // Overall Progress Box
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, NovaBorder)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "${(progress.overallProgress * 100).toInt()}% Transferred",
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = NovaTextPrimary
            )
            Text(
              text = "${formatBytes(progress.transferredBytes)} / ${formatBytes(progress.totalBytes)}",
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              color = NovaTextSecondary
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          LinearProgressIndicator(
            progress = { progress.overallProgress },
            modifier = Modifier
              .fillMaxWidth()
              .height(10.dp)
              .clip(RoundedCornerShape(5.dp)),
            color = if (progress.state == EngineState.COMPLETED) NovaSuccess else NovaPrimary,
            trackColor = NovaPrimaryLight,
            strokeCap = StrokeCap.Round
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "File ${progress.currentFileIndex} of ${progress.totalFiles}: ${progress.currentFileName}",
              fontSize = 12.sp,
              color = NovaTextSecondary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f)
            )
            Text(
              text = progress.securityProtocol,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              color = NovaTextMuted
            )
          }
        }
      }
    }

    // Individual Files Progress
    if (progress.filesProgress.isNotEmpty()) {
      item {
        Text(
          text = "Item-by-Item Verification Queue",
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          color = NovaTextPrimary
        )
      }

      items(progress.filesProgress, key = { it.id }) { file ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.dp, NovaBorder)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                  imageVector = getFileTypeIcon(file.fileType),
                  contentDescription = null,
                  tint = getFileTypeColor(file.fileType),
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = file.fileName,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = NovaTextPrimary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
              Text(
                text = "${(file.progress * 100).toInt()}% • ${file.status}",
                fontSize = 11.sp,
                color = if (file.status == "COMPLETED") NovaSuccess else NovaPrimary,
                fontWeight = FontWeight.SemiBold
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
              progress = { file.progress },
              modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
              color = if (file.status == "COMPLETED") NovaSuccess else NovaPrimary,
              trackColor = Color(0xFFF1F5F9)
            )
          }
        }
      }
    }

    // Completed State Box or Controls
    item {
      if (progress.state == EngineState.COMPLETED) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = NovaSuccessBg),
          border = BorderStroke(1.5.dp, NovaSuccess)
        ) {
          Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = NovaSuccess,
              modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Transfer Successfully Completed!",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = NovaSuccess
            )
            Text(
              text = "SHA-256 integrity match confirmed on recipient device.",
              fontSize = 12.sp,
              color = NovaTextSecondary,
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedButton(
                onClick = onSendAnother,
                modifier = Modifier.weight(1f).height(42.dp),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("Send Another", fontSize = 12.sp)
              }
              Button(
                onClick = onDone,
                colors = ButtonDefaults.buttonColors(containerColor = NovaSuccess),
                modifier = Modifier.weight(1f).height(42.dp),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("Done", fontSize = 12.sp)
              }
            }
          }
        }
      } else {
        // Active Controls (Pause, Resume, Cancel)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = onPauseToggle,
            modifier = Modifier.weight(1f).height(44.dp),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, NovaBorder)
          ) {
            Icon(
              imageVector = if (progress.state == EngineState.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause,
              contentDescription = null,
              tint = NovaPrimary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (progress.state == EngineState.PAUSED) "Resume" else "Pause", fontSize = 13.sp)
          }

          Button(
            onClick = onCancel,
            colors = ButtonDefaults.buttonColors(containerColor = NovaError),
            modifier = Modifier.weight(1f).height(44.dp),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Cancel", fontSize = 13.sp)
          }
        }
      }
    }
  }
}

@Composable
private fun AddCustomFileDialog(
  onDismiss: () -> Unit,
  onAdd: (name: String, sizeBytes: Long, type: String) -> Unit
) {
  var fileName by remember { mutableStateOf("Shared_Document.pdf") }
  var fileSizeMb by remember { mutableStateOf("25") }
  var fileType by remember { mutableStateOf("DOCUMENT") }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = Color.White,
      border = BorderStroke(1.dp, NovaBorder),
      modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Text("Add Custom Content", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NovaTextPrimary)
        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = fileName,
          onValueChange = { fileName = it },
          label = { Text("File Name with Extension") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = fileSizeMb,
          onValueChange = { fileSizeMb = it },
          label = { Text("Estimated Size (MB)") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NovaTextPrimary)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf("DOCUMENT", "IMAGE", "VIDEO", "ARCHIVE").forEach { cat ->
            val isCatSelected = fileType == cat
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isCatSelected) NovaPrimary else Color(0xFFF1F5F9))
                .clickable { fileType = cat }
                .padding(vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = cat.take(3),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCatSelected) Color.White else NovaTextSecondary
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
            Text("Cancel")
          }
          Button(
            onClick = {
              val sizeMb = fileSizeMb.toLongOrNull() ?: 10L
              onAdd(fileName, sizeMb * 1024 * 1024L, fileType)
            },
            colors = ButtonDefaults.buttonColors(containerColor = NovaPrimary),
            modifier = Modifier.weight(1f)
          ) {
            Text("Add Item")
          }
        }
      }
    }
  }
}
