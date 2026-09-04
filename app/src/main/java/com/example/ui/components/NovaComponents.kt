package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tablet
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.NotificationEntity
import com.example.data.model.TransferEntity
import com.example.engine.ActiveTransferProgress
import com.example.engine.EngineState
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaError
import com.example.ui.theme.NovaErrorBg
import com.example.ui.theme.NovaPrimary
import com.example.ui.theme.NovaPrimaryLight
import com.example.ui.theme.NovaSuccess
import com.example.ui.theme.NovaSuccessBg
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.theme.NovaWarning
import com.example.ui.theme.NovaWarningBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatBytes(bytes: Long): String {
  if (bytes < 1024) return "$bytes B"
  val kb = bytes / 1024.0
  if (kb < 1024) return "${"%.1f".format(kb)} KB"
  val mb = kb / 1024.0
  if (mb < 1024) return "${"%.1f".format(mb)} MB"
  val gb = mb / 1024.0
  return "${"%.2f".format(gb)} GB"
}

fun formatTimestamp(millis: Long): String {
  val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
  return sdf.format(Date(millis))
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
  val (bgColor, textColor, icon) = when (status) {
    "COMPLETED" -> Triple(NovaSuccessBg, NovaSuccess, Icons.Default.CheckCircle)
    "TRANSFERRING", "CONNECTING", "ACCEPTED" -> Triple(NovaPrimaryLight, NovaPrimary, Icons.Default.Sync)
    "WAITING", "WAITING_ACCEPTANCE", "PENDING" -> Triple(NovaWarningBg, NovaWarning, Icons.Default.Security)
    "FAILED", "CANCELLED" -> Triple(NovaErrorBg, NovaError, Icons.Default.Error)
    else -> Triple(Color(0xFFF1F5F9), NovaTextSecondary, Icons.Default.Description)
  }

  Row(
    modifier = modifier
      .clip(RoundedCornerShape(100.dp))
      .background(bgColor)
      .padding(horizontal = 8.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = textColor,
      modifier = Modifier.size(12.dp)
    )
    Text(
      text = status.replace("_", " "),
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      color = textColor
    )
  }
}

@Composable
fun UserAvatar(
  name: String,
  initials: String,
  colorHex: Long,
  size: Dp = 44.dp,
  isOnline: Boolean? = null,
  isVerified: Boolean = false,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    Box(
      modifier = Modifier
        .size(size)
        .clip(CircleShape)
        .background(Color(colorHex)),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = initials.take(2).uppercase(),
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = (size.value * 0.38f).sp
      )
    }

    if (isOnline != null) {
      Box(
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .size((size.value * 0.32f).dp.coerceAtLeast(10.dp))
          .clip(CircleShape)
          .border(2.dp, Color.White, CircleShape)
          .background(if (isOnline) NovaSuccess else Color(0xFF94A3B8))
      )
    }

    if (isVerified) {
      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .size((size.value * 0.35f).dp.coerceAtLeast(12.dp))
          .clip(CircleShape)
          .background(Color.White)
          .border(1.dp, Color(0xFFE2E8F0), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Verified,
          contentDescription = "Verified",
          tint = NovaPrimary,
          modifier = Modifier.size((size.value * 0.28f).dp)
        )
      }
    }
  }
}

@Composable
fun getFileTypeIcon(type: String): ImageVector {
  return when (type.uppercase()) {
    "VIDEO" -> Icons.Default.VideoFile
    "IMAGE", "PHOTO" -> Icons.Default.Image
    "AUDIO" -> Icons.Default.AudioFile
    "ARCHIVE", "ZIP", "FOLDER" -> Icons.Default.FolderZip
    "CODE", "TEXT" -> Icons.Default.Code
    else -> Icons.Default.Description
  }
}

@Composable
fun getFileTypeColor(type: String): Color {
  return when (type.uppercase()) {
    "VIDEO" -> Color(0xFFEF4444)
    "IMAGE", "PHOTO" -> Color(0xFF10B981)
    "AUDIO" -> Color(0xFF8B5CF6)
    "ARCHIVE", "ZIP", "FOLDER" -> Color(0xFFF59E0B)
    "CODE", "TEXT" -> Color(0xFF0EA5E9)
    else -> NovaPrimary
  }
}

@Composable
fun getDeviceIcon(type: String): ImageVector {
  return when (type.uppercase()) {
    "LAPTOP", "DESKTOP" -> Icons.Default.Computer
    "TABLET" -> Icons.Default.Tablet
    "SERVER" -> Icons.Default.Storage
    else -> Icons.Default.PhoneAndroid
  }
}

@Composable
fun MetricCard(
  title: String,
  value: String,
  subtitle: String,
  icon: ImageVector,
  iconTint: Color,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.NovaSurfaceVariant),
    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.NovaBorderSubtle)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title.uppercase(),
          fontSize = 11.sp,
          color = NovaTextSecondary,
          fontWeight = FontWeight.Medium,
          letterSpacing = 0.8.sp
        )
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
          )
        }
      }
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = value,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = NovaTextPrimary
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitle,
        fontSize = 12.sp,
        color = NovaTextSecondary
      )
    }
  }
}

@Composable
fun LiveTransferBanner(
  progress: ActiveTransferProgress,
  onPauseToggle: () -> Unit,
  onCancel: () -> Unit,
  onViewDetails: () -> Unit,
  modifier: Modifier = Modifier
) {
  if (progress.state == EngineState.IDLE || progress.state == EngineState.COMPLETED) {
    return
  }

  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(800),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_alpha"
  )

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clickable { onViewDetails() }
      .testTag("live_transfer_banner"),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = androidx.compose.foundation.BorderStroke(1.dp, NovaBorder)
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(NovaPrimary.copy(alpha = pulseAlpha))
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = if (progress.direction == "SEND") "Sending to ${progress.partnerName}" else "Receiving from ${progress.partnerName}",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = NovaTextPrimary
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          IconButton(
            onClick = onPauseToggle,
            modifier = Modifier.size(34.dp).testTag("pause_transfer_btn")
          ) {
            Icon(
              imageVector = if (progress.state == EngineState.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause,
              contentDescription = "Pause/Resume",
              tint = NovaPrimary,
              modifier = Modifier.size(18.dp)
            )
          }
          IconButton(
            onClick = onCancel,
            modifier = Modifier.size(34.dp).testTag("cancel_transfer_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Cancel",
              tint = NovaError,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      LinearProgressIndicator(
        progress = { progress.overallProgress },
        modifier = Modifier
          .fillMaxWidth()
          .height(8.dp)
          .clip(RoundedCornerShape(4.dp)),
        color = NovaPrimary,
        trackColor = NovaPrimaryLight,
        strokeCap = StrokeCap.Round
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "${formatBytes(progress.transferredBytes)} / ${formatBytes(progress.totalBytes)} (${(progress.overallProgress * 100).toInt()}%)",
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          color = NovaTextSecondary
        )
        Text(
          text = if (progress.speedMbPerSec > 0) "${"%.1f".format(progress.speedMbPerSec)} MB/s • ${progress.etaSeconds}s left" else progress.statusMessage,
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = NovaPrimary
        )
      }
    }
  }
}

@Composable
fun TransferDetailsModal(
  transfer: TransferEntity,
  onDismiss: () -> Unit,
  onDelete: () -> Unit,
  onRetry: (() -> Unit)? = null
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = Color.White,
      border = androidx.compose.foundation.BorderStroke(1.dp, NovaBorder),
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 16.dp)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Transfer Details",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = NovaTextPrimary
            )
            Text(
              text = "ID: ${transfer.id}",
              fontSize = 12.sp,
              fontFamily = FontFamily.Monospace,
              color = NovaTextMuted
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = NovaTextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Direction & Status Card
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, NovaBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = if (transfer.direction == "SEND") "Outgoing Transfer" else "Incoming Transfer",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = NovaTextPrimary
              )
              Text(
                text = "${transfer.senderName} (${transfer.senderDevice}) → ${transfer.receiverName} (${transfer.receiverDevice})",
                fontSize = 12.sp,
                color = NovaTextSecondary
              )
            }
            StatusBadge(status = transfer.status)
          }
        }

        if (!transfer.failureReason.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(10.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(NovaErrorBg)
              .border(1.dp, NovaError.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
              .padding(12.dp)
          ) {
            Row(verticalAlignment = Alignment.Top) {
              Icon(Icons.Default.Error, contentDescription = null, tint = NovaError, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Transfer Failure Log",
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = NovaError
                )
                Text(
                  text = transfer.failureReason ?: "",
                  fontSize = 11.sp,
                  color = NovaTextPrimary,
                  modifier = Modifier.padding(top = 2.dp)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Technical Specs
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          DetailRow(label = "Total Size", value = formatBytes(transfer.totalBytes))
          DetailRow(label = "Files Count", value = "${transfer.totalFiles} item(s)")
          DetailRow(label = "Average Speed", value = transfer.speed)
          DetailRow(label = "Created", value = formatTimestamp(transfer.createdAt))
          if (transfer.completedAt != null) {
            DetailRow(label = "Completed", value = formatTimestamp(transfer.completedAt))
          }
          DetailRow(label = "Security Protocol", value = transfer.securityStatus)
          DetailRow(
            label = "SHA-256 Hash",
            value = transfer.checksum.take(22) + "...",
            isMonospace = true
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Content Manifest:",
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.sp,
          color = NovaTextPrimary
        )
        Text(
          text = transfer.fileNamesSummary,
          fontSize = 12.sp,
          color = NovaTextSecondary,
          modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = {
              onDelete()
              onDismiss()
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NovaError),
            modifier = Modifier.weight(1f).height(44.dp)
          ) {
            Text("Delete Log", fontSize = 13.sp)
          }

          if (transfer.status in listOf("FAILED", "CANCELLED") && onRetry != null) {
            Button(
              onClick = {
                onRetry()
                onDismiss()
              },
              colors = ButtonDefaults.buttonColors(containerColor = NovaPrimary),
              modifier = Modifier.weight(1f).height(44.dp)
            ) {
              Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Retry", fontSize = 13.sp)
            }
          } else {
            Button(
              onClick = onDismiss,
              colors = ButtonDefaults.buttonColors(containerColor = NovaPrimary),
              modifier = Modifier.weight(1f).height(44.dp)
            ) {
              Text("Done", fontSize = 13.sp)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun DetailRow(label: String, value: String, isMonospace: Boolean = false) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(text = label, fontSize = 12.sp, color = NovaTextMuted)
    Text(
      text = value,
      fontSize = 12.sp,
      fontWeight = FontWeight.Medium,
      color = NovaTextPrimary,
      fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default
    )
  }
}

@Composable
fun NotificationsDialog(
  notifications: List<NotificationEntity>,
  onDismiss: () -> Unit,
  onMarkAllRead: () -> Unit,
  onClearAll: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = Color.White,
      border = androidx.compose.foundation.BorderStroke(1.dp, NovaBorder),
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 24.dp)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Notifications",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = NovaTextPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(NovaPrimaryLight)
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "${notifications.size}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NovaPrimary
              )
            }
          }

          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = NovaTextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (notifications.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            contentAlignment = Alignment.Center
          ) {
            Text("No new notifications", color = NovaTextMuted, fontSize = 14.sp)
          }
        } else {
          Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            notifications.take(5).forEach { notif ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(if (!notif.isRead) Color(0xFFF1F5F9) else Color.White)
                  .border(1.dp, NovaBorder, RoundedCornerShape(10.dp))
                  .padding(10.dp),
                verticalAlignment = Alignment.Top
              ) {
                Box(
                  modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(NovaPrimaryLight),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = when (notif.type) {
                      "TRANSFER_COMPLETED" -> Icons.Default.CheckCircle
                      "DEVICE_CONNECTED" -> Icons.Default.Computer
                      "SECURITY" -> Icons.Default.Security
                      else -> Icons.Default.Sync
                    },
                    contentDescription = null,
                    tint = NovaPrimary,
                    modifier = Modifier.size(16.dp)
                  )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = notif.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = NovaTextPrimary
                  )
                  Text(
                    text = notif.body,
                    fontSize = 12.sp,
                    color = NovaTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = formatTimestamp(notif.timestamp),
                    fontSize = 10.sp,
                    color = NovaTextMuted,
                    modifier = Modifier.padding(top = 2.dp)
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = onClearAll,
            modifier = Modifier.weight(1f).height(40.dp)
          ) {
            Text("Clear All", fontSize = 12.sp)
          }

          Button(
            onClick = {
              onMarkAllRead()
              onDismiss()
            },
            colors = ButtonDefaults.buttonColors(containerColor = NovaPrimary),
            modifier = Modifier.weight(1f).height(40.dp)
          ) {
            Text("Mark Read", fontSize = 12.sp)
          }
        }
      }
    }
  }
}
