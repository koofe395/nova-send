package com.example.ui.screens

import android.content.Context
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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TransferEntity
import com.example.ui.NovaViewModel
import com.example.ui.components.LiveTransferBanner
import com.example.ui.components.StatusBadge
import com.example.ui.components.TransferDetailsModal
import com.example.ui.components.UserAvatar
import com.example.ui.components.formatBytes
import com.example.ui.components.formatTimestamp
import com.example.ui.components.getFileTypeColor
import com.example.ui.components.getFileTypeIcon
import com.example.ui.theme.NovaAccent
import com.example.ui.theme.NovaBackground
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaBorderSubtle
import com.example.ui.theme.NovaCardSurface
import com.example.ui.theme.NovaError
import com.example.ui.theme.NovaErrorBg
import com.example.ui.theme.NovaPrimary
import com.example.ui.theme.NovaPrimaryContainer
import com.example.ui.theme.NovaPrimaryLight
import com.example.ui.theme.NovaSuccess
import com.example.ui.theme.NovaSuccessBg
import com.example.ui.theme.NovaSurfaceVariant
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.util.QuickTransferNotificationManager

@Composable
fun OverviewScreen(
  viewModel: NovaViewModel,
  onNavigateToSend: () -> Unit,
  onNavigateToReceive: () -> Unit,
  onNavigateToDevices: () -> Unit,
  onNavigateToMessages: () -> Unit,
  onOpenQrDialog: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val allTransfers by viewModel.allTransfers.collectAsStateWithLifecycle()
  val successfulTransfers by viewModel.successfulTransfers.collectAsStateWithLifecycle()
  val failedTransfers by viewModel.failedTransfers.collectAsStateWithLifecycle()
  val activeProgress by viewModel.activeProgress.collectAsStateWithLifecycle()
  val incomingTransfers by viewModel.incomingTransfers.collectAsStateWithLifecycle()
  val inspectedTransfer by viewModel.inspectedTransfer.collectAsStateWithLifecycle()

  var selectedFilter by remember { mutableStateOf("ALL") } // ALL, SUCCESSFUL, FAILED, SENT, RECEIVED
  var isPersistentNotificationActive by remember {
    mutableStateOf(QuickTransferNotificationManager.isPersistentNotificationEnabled(context))
  }

  val totalSentBytes = remember(allTransfers) {
    allTransfers.filter { it.direction == "SEND" && it.status == "COMPLETED" }.sumOf { it.totalBytes }
  }
  val totalReceivedBytes = remember(allTransfers) {
    allTransfers.filter { it.direction == "RECEIVE" && it.status == "COMPLETED" }.sumOf { it.totalBytes }
  }

  val displayTransfers = remember(allTransfers, successfulTransfers, failedTransfers, selectedFilter) {
    when (selectedFilter) {
      "SUCCESSFUL" -> successfulTransfers
      "FAILED" -> failedTransfers
      "SENT" -> allTransfers.filter { it.direction == "SEND" }
      "RECEIVED" -> allTransfers.filter { it.direction == "RECEIVE" }
      else -> allTransfers
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(NovaBackground)
      .testTag("overview_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Header Greeting & Quick Pairing Button
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Good morning, Mohamed",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = NovaTextPrimary
          )
          Text(
            text = "Vencold Cloud & P2P Storage Ledger",
            fontSize = 13.sp,
            color = NovaTextSecondary
          )
        }

        // QR Code Pairing Action Button
        Surface(
          modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .clickable { onOpenQrDialog() }
            .testTag("header_qr_pair_btn"),
          color = NovaPrimaryLight,
          border = BorderStroke(1.dp, NovaPrimary.copy(alpha = 0.2f))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.QrCodeScanner,
              contentDescription = "Scan or Pair",
              tint = NovaPrimary,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = "Pair QR",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = NovaPrimary
            )
          }
        }
      }
    }

    // 2. Vencold Cloud Storage Hero Card (Dribbble 18492081 Reference)
    item {
      VencoldStorageCard(
        usedBytes = 58_400_000_000L + totalSentBytes + totalReceivedBytes,
        totalCapacityBytes = 128_000_000_000L,
        onCleanStorage = { viewModel.clearAllHistory() },
        onQuickSend = onNavigateToSend
      )
    }

    // 3. Vencold 4-Folder Categories Grid
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "FILE CATEGORIES",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = NovaTextSecondary,
          letterSpacing = 0.8.sp
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          VencoldCategoryCard(
            title = "Documents",
            filesCount = "128 files",
            sizeText = "14.2 GB",
            icon = Icons.Default.Description,
            color = Color(0xFF6366F1),
            modifier = Modifier.weight(1f)
          )
          VencoldCategoryCard(
            title = "Images & Media",
            filesCount = "1,420 files",
            sizeText = "28.5 GB",
            icon = Icons.Default.Image,
            color = Color(0xFF10B981),
            modifier = Modifier.weight(1f)
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          VencoldCategoryCard(
            title = "Audio & Video",
            filesCount = "84 files",
            sizeText = "12.6 GB",
            icon = Icons.Default.VideoFile,
            color = Color(0xFF8B5CF6),
            modifier = Modifier.weight(1f)
          )
          VencoldCategoryCard(
            title = "Archives & Code",
            filesCount = "32 files",
            sizeText = "3.1 GB",
            icon = Icons.Default.Archive,
            color = Color(0xFFF59E0B),
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    // 4. Background Persistent Notification Access Banner
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("persistent_notification_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, NovaBorderSubtle)
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
                .size(42.dp)
                .clip(CircleShape)
                .background(if (isPersistentNotificationActive) NovaPrimaryLight else NovaSurfaceVariant),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = if (isPersistentNotificationActive) NovaPrimary else NovaTextMuted,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Quick Send Notification",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = NovaTextPrimary
              )
              Text(
                text = if (isPersistentNotificationActive) "Active in notification tray" else "Access file picker while multitasking",
                fontSize = 11.sp,
                color = NovaTextSecondary
              )
            }
          }

          Switch(
            checked = isPersistentNotificationActive,
            onCheckedChange = { active ->
              isPersistentNotificationActive = active
              if (active) {
                QuickTransferNotificationManager.showPersistentNotification(context)
              } else {
                QuickTransferNotificationManager.cancelPersistentNotification(context)
              }
            },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = NovaPrimary,
              uncheckedThumbColor = Color.White,
              uncheckedTrackColor = NovaSurfaceVariant
            )
          )
        }
      }
    }

    // 5. Active Live Transfer Banner (if in progress)
    item {
      LiveTransferBanner(
        progress = activeProgress,
        onPauseToggle = { viewModel.togglePauseTransfer() },
        onCancel = { viewModel.cancelActiveTransfer() },
        onViewDetails = { onNavigateToSend() }
      )
    }

    // 6. Incoming Transfer Request Card (if any pending)
    if (incomingTransfers.isNotEmpty()) {
      item {
        val incoming = incomingTransfers.first()
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("incoming_request_card"),
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.5.dp, NovaPrimary)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(
                  name = incoming.senderName,
                  initials = incoming.senderName.take(2),
                  colorHex = incoming.avatarColorHex,
                  size = 40.dp,
                  isVerified = true
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "${incoming.senderName} wants to send files",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NovaTextPrimary
                  )
                  Text(
                    text = "${incoming.senderDevice} • @${incoming.senderUsername}",
                    fontSize = 12.sp,
                    color = NovaTextMuted
                  )
                }
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(NovaPrimaryLight)
                  .padding(horizontal = 10.dp, vertical = 5.dp)
              ) {
                Text(
                  text = formatBytes(incoming.totalBytes),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = NovaPrimary
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "Files: ${incoming.filesSummary}",
              fontSize = 12.sp,
              color = NovaTextSecondary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              OutlinedButton(
                onClick = { viewModel.declineIncoming(incoming) },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, NovaBorder)
              ) {
                Text("Decline", fontSize = 13.sp, color = NovaTextSecondary)
              }

              Button(
                onClick = {
                  viewModel.acceptIncoming(incoming)
                  onNavigateToReceive()
                },
                modifier = Modifier.weight(1f).height(44.dp).testTag("accept_incoming_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NovaPrimary)
              ) {
                Text("Accept Transfer", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
              }
            }
          }
        }
      }
    }

    // 7. Quick Navigation Action Grid (Send, Receive, QR Pair, Devices)
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        ActionBigCard(
          title = "SEND FILES",
          subtitle = "P2P Wi-Fi Direct",
          icon = Icons.Default.Upload,
          color = NovaPrimary,
          modifier = Modifier.weight(1f).testTag("send_file_card"),
          onClick = onNavigateToSend
        )

        ActionBigCard(
          title = "RECEIVE",
          subtitle = "Radar listening",
          icon = Icons.Default.Download,
          color = NovaPrimary,
          modifier = Modifier.weight(1f).testTag("receive_file_card"),
          onClick = onNavigateToReceive
        )
      }
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        ActionBigCard(
          title = "QR PAIRING",
          subtitle = "Scan device QR",
          icon = Icons.Default.QrCodeScanner,
          color = NovaPrimary,
          modifier = Modifier.weight(1f).testTag("qr_pair_card"),
          onClick = onOpenQrDialog
        )

        ActionBigCard(
          title = "TRUSTED NODES",
          subtitle = "Paired hardware",
          icon = Icons.Default.Devices,
          color = NovaPrimary,
          modifier = Modifier.weight(1f).testTag("devices_card"),
          onClick = onNavigateToDevices
        )
      }
    }

    // 8. Room Activity Ledger Section & High-Contrast Filter Chips
    item {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "RECENT ACTIVITY",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = NovaTextSecondary,
              letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = CircleShape,
              color = NovaPrimaryLight
            ) {
              Text(
                text = "${allTransfers.size}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NovaPrimary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Pills: All, Successful, Failed, Sent, Received
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          FilterChipItem(
            text = "All",
            isSelected = selectedFilter == "ALL",
            onClick = { selectedFilter = "ALL" }
          )
          FilterChipItem(
            text = "Successful (${successfulTransfers.size})",
            isSelected = selectedFilter == "SUCCESSFUL",
            onClick = { selectedFilter = "SUCCESSFUL" },
            accentColor = NovaSuccess
          )
          FilterChipItem(
            text = "Failed (${failedTransfers.size})",
            isSelected = selectedFilter == "FAILED",
            onClick = { selectedFilter = "FAILED" },
            accentColor = NovaError
          )
          FilterChipItem(
            text = "Sent",
            isSelected = selectedFilter == "SENT",
            onClick = { selectedFilter = "SENT" }
          )
          FilterChipItem(
            text = "Received",
            isSelected = selectedFilter == "RECEIVED",
            onClick = { selectedFilter = "RECEIVED" }
          )
        }
      }
    }

    // 9. Activity Ledger Items
    if (displayTransfers.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.dp, NovaBorderSubtle)
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = null,
                tint = NovaTextMuted,
                modifier = Modifier.size(32.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text("No transfers recorded in this category", fontWeight = FontWeight.Medium, color = NovaTextPrimary)
              Text("Transfers logged in local Room database will appear here", fontSize = 12.sp, color = NovaTextSecondary)
            }
          }
        }
      }
    } else {
      items(displayTransfers.take(6), key = { it.id }) { transfer ->
        TransferHistoryItemRow(
          transfer = transfer,
          onClick = { viewModel.inspectTransfer(transfer) },
          onRetry = { viewModel.retryTransfer(transfer) }
        )
      }
    }
  }

  // Transfer Inspection Details Modal
  inspectedTransfer?.let { transfer ->
    TransferDetailsModal(
      transfer = transfer,
      onDismiss = { viewModel.inspectTransfer(null) },
      onDelete = { viewModel.deleteTransfer(transfer.id) },
      onRetry = { viewModel.retryTransfer(transfer) }
    )
  }
}

/**
 * Vencold Cloud Storage Overview Card with segmented visual progress bar and category dots.
 */
@Composable
private fun VencoldStorageCard(
  usedBytes: Long,
  totalCapacityBytes: Long,
  onCleanStorage: () -> Unit,
  onQuickSend: () -> Unit
) {
  val usedFrac = (usedBytes.toFloat() / totalCapacityBytes.toFloat()).coerceIn(0f, 1f)

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("vencold_storage_card"),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = BorderStroke(1.dp, NovaBorderSubtle),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(20.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Storage Overview",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = NovaTextPrimary
          )
          Text(
            text = "${formatBytes(usedBytes)} of ${formatBytes(totalCapacityBytes)} used",
            fontSize = 12.sp,
            color = NovaTextSecondary
          )
        }

        Surface(
          color = NovaPrimaryLight,
          shape = RoundedCornerShape(100.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.CloudDone, contentDescription = null, tint = NovaPrimary, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${(usedFrac * 100).toInt()}% Used",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = NovaPrimary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Multi-segmented Progress Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(10.dp)
          .clip(RoundedCornerShape(5.dp))
          .background(NovaSurfaceVariant)
      ) {
        Box(
          modifier = Modifier
            .weight(0.25f)
            .fillMaxSize()
            .background(Color(0xFF6366F1)) // Documents
        )
        Spacer(modifier = Modifier.width(2.dp))
        Box(
          modifier = Modifier
            .weight(0.35f)
            .fillMaxSize()
            .background(Color(0xFF10B981)) // Media
        )
        Spacer(modifier = Modifier.width(2.dp))
        Box(
          modifier = Modifier
            .weight(0.20f)
            .fillMaxSize()
            .background(Color(0xFF8B5CF6)) // Video
        )
        Spacer(modifier = Modifier.width(2.dp))
        Box(
          modifier = Modifier
            .weight(0.10f)
            .fillMaxSize()
            .background(Color(0xFFF59E0B)) // Archive
        )
        Spacer(modifier = Modifier.width(2.dp))
        Box(
          modifier = Modifier
            .weight(0.10f)
            .fillMaxSize()
            .background(Color(0xFFCBD5E1)) // Free
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Category Legend Dots
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        StorageCategoryDot(label = "Docs", color = Color(0xFF6366F1))
        StorageCategoryDot(label = "Media", color = Color(0xFF10B981))
        StorageCategoryDot(label = "Video", color = Color(0xFF8B5CF6))
        StorageCategoryDot(label = "Zip", color = Color(0xFFF59E0B))
        StorageCategoryDot(label = "Free", color = Color(0xFF94A3B8))
      }
    }
  }
}

@Composable
private fun StorageCategoryDot(label: String, color: Color) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(8.dp)
        .clip(CircleShape)
        .background(color)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(text = label, fontSize = 11.sp, color = NovaTextSecondary)
  }
}

/**
 * Vencold 4-Folder Category Tile
 */
@Composable
private fun VencoldCategoryCard(
  title: String,
  filesCount: String,
  sizeText: String,
  icon: ImageVector,
  color: Color,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = BorderStroke(1.dp, NovaBorderSubtle)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        }

        Text(
          text = sizeText,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = NovaTextPrimary
        )
      }

      Spacer(modifier = Modifier.height(10.dp))
      Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NovaTextPrimary)
      Text(text = filesCount, fontSize = 11.sp, color = NovaTextMuted)
    }
  }
}

@Composable
private fun ActionBigCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  color: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Card(
    modifier = modifier.clickable { onClick() },
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = BorderStroke(1.dp, NovaBorderSubtle)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(NovaPrimaryLight),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
      }
      Spacer(modifier = Modifier.height(12.dp))
      Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NovaTextPrimary)
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitle,
        fontSize = 11.sp,
        color = NovaTextSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
private fun FilterChipItem(
  text: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  accentColor: Color? = null
) {
  val chipBg = if (isSelected) {
    accentColor ?: NovaPrimary
  } else {
    NovaSurfaceVariant
  }

  val chipText = if (isSelected) {
    Color.White
  } else {
    accentColor ?: NovaTextSecondary
  }

  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(100.dp))
      .background(chipBg)
      .border(1.dp, if (isSelected) chipBg else NovaBorderSubtle, RoundedCornerShape(100.dp))
      .clickable { onClick() }
      .padding(horizontal = 12.dp, vertical = 6.dp)
  ) {
    Text(
      text = text,
      fontSize = 11.sp,
      fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
      color = chipText
    )
  }
}

@Composable
fun TransferHistoryItemRow(
  transfer: TransferEntity,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  onRetry: (() -> Unit)? = null
) {
  val isFailed = transfer.status in listOf("FAILED", "CANCELLED")

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("transfer_row_${transfer.id}"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = BorderStroke(1.dp, if (isFailed) NovaError.copy(alpha = 0.35f) else NovaBorderSubtle)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(if (isFailed) NovaErrorBg else getFileTypeColor(transfer.primaryFileType).copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isFailed) Icons.Default.Error else getFileTypeIcon(transfer.primaryFileType),
              contentDescription = null,
              tint = if (isFailed) NovaError else getFileTypeColor(transfer.primaryFileType),
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = transfer.fileNamesSummary,
              fontWeight = FontWeight.SemiBold,
              fontSize = 13.sp,
              color = NovaTextPrimary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = if (transfer.direction == "SEND") "To ${transfer.receiverName} • ${formatBytes(transfer.totalBytes)}" else "From ${transfer.senderName} • ${formatBytes(transfer.totalBytes)}",
              fontSize = 11.sp,
              color = NovaTextSecondary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
          StatusBadge(status = transfer.status)
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = formatTimestamp(transfer.createdAt),
            fontSize = 10.sp,
            color = NovaTextMuted
          )
        }
      }

      // If failed, show failure reason and Quick Retry Button
      if (isFailed) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(NovaErrorBg)
            .padding(horizontal = 10.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = transfer.failureReason ?: "Transfer connection failed",
            fontSize = 11.sp,
            color = NovaError,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
          )
          if (onRetry != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
              modifier = Modifier.clickable { onRetry() },
              color = NovaError,
              shape = RoundedCornerShape(100.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Retry", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }
}
