package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.EngineState
import com.example.ui.NovaViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.components.TransferDetailsModal
import com.example.ui.components.formatBytes
import com.example.ui.components.formatTimestamp
import com.example.ui.components.getFileTypeColor
import com.example.ui.components.getFileTypeIcon
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaError
import com.example.ui.theme.NovaPrimary
import com.example.ui.theme.NovaPrimaryLight
import com.example.ui.theme.NovaSuccess
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary

@Composable
fun TransfersScreen(
  viewModel: NovaViewModel,
  modifier: Modifier = Modifier
) {
  val allTransfers by viewModel.allTransfers.collectAsStateWithLifecycle()
  val activeProgress by viewModel.activeProgress.collectAsStateWithLifecycle()
  val inspectedTransfer by viewModel.inspectedTransfer.collectAsStateWithLifecycle()

  var currentTab by remember { mutableStateOf("ALL") } // ALL, ACTIVE, COMPLETED

  val filteredList = remember(allTransfers, currentTab, activeProgress) {
    when (currentTab) {
      "ACTIVE" -> allTransfers.filter { it.status in listOf("TRANSFERRING", "CONNECTING", "WAITING", "ACCEPTED") }
      "COMPLETED" -> allTransfers.filter { it.status == "COMPLETED" }
      else -> allTransfers
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(com.example.ui.theme.NovaBackground)
      .testTag("transfers_center_screen")
  ) {
    // Top Tabs
    Surface(
      color = Color.White,
      border = BorderStroke(1.dp, com.example.ui.theme.NovaBorderSubtle),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf(
          Pair("ALL", "All (${allTransfers.size + if (activeProgress.state != EngineState.IDLE) 1 else 0})"),
          Pair("ACTIVE", "Active (${if (activeProgress.state in listOf(EngineState.TRANSFERRING, EngineState.CONNECTING, EngineState.PAUSED)) 1 else 0})"),
          Pair("COMPLETED", "Completed (${allTransfers.count { it.status == "COMPLETED" }})")
        ).forEach { (tabKey, title) ->
          val isSelected = currentTab == tabKey
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(100.dp))
              .background(if (isSelected) NovaPrimary else com.example.ui.theme.NovaSurfaceVariant)
              .clickable { currentTab = tabKey }
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = title,
              fontSize = 11.sp,
              fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
              color = if (isSelected) Color.White else NovaTextSecondary,
              textAlign = TextAlign.Center
            )
          }
        }
      }
    }

    // Transfers List
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Live In-Progress Card if active and matches filter
      if ((currentTab == "ALL" || currentTab == "ACTIVE") &&
        activeProgress.state != EngineState.IDLE && activeProgress.state != EngineState.COMPLETED
      ) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.5.dp, NovaPrimary)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = if (activeProgress.direction == "SEND") "Outgoing → ${activeProgress.partnerName}" else "Incoming ← ${activeProgress.partnerName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NovaTextPrimary
                  )
                  Text(
                    text = "${activeProgress.currentFileName} • ${activeProgress.partnerDevice}",
                    fontSize = 11.sp,
                    color = NovaTextSecondary
                  )
                }

                Row {
                  IconButton(
                    onClick = { viewModel.togglePauseTransfer() },
                    modifier = Modifier.size(32.dp)
                  ) {
                    Icon(
                      imageVector = if (activeProgress.state == EngineState.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause,
                      contentDescription = null,
                      tint = NovaPrimary,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                  IconButton(
                    onClick = { viewModel.cancelActiveTransfer() },
                    modifier = Modifier.size(32.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Close,
                      contentDescription = null,
                      tint = NovaError,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              LinearProgressIndicator(
                progress = { activeProgress.overallProgress },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(8.dp)
                  .clip(RoundedCornerShape(4.dp)),
                color = NovaPrimary,
                trackColor = NovaPrimaryLight,
                strokeCap = StrokeCap.Round
              )

              Spacer(modifier = Modifier.height(8.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "${formatBytes(activeProgress.transferredBytes)} / ${formatBytes(activeProgress.totalBytes)} (${(activeProgress.overallProgress * 100).toInt()}%)",
                  fontSize = 11.sp,
                  color = NovaTextSecondary
                )
                Text(
                  text = if (activeProgress.speedMbPerSec > 0) "${"%.1f".format(activeProgress.speedMbPerSec)} MB/s" else activeProgress.statusMessage,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = NovaPrimary
                )
              }
            }
          }
        }
      }

      if (filteredList.isEmpty() && activeProgress.state == EngineState.IDLE) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, NovaBorder)
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(36.dp),
              contentAlignment = Alignment.Center
            ) {
              Text("No transfers found in this tab", color = NovaTextMuted, fontSize = 13.sp)
            }
          }
        }
      } else {
        items(filteredList, key = { it.id }) { transfer ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { viewModel.inspectTransfer(transfer) },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, NovaBorder)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(getFileTypeColor(transfer.primaryFileType).copy(alpha = 0.12f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = getFileTypeIcon(transfer.primaryFileType),
                    contentDescription = null,
                    tint = getFileTypeColor(transfer.primaryFileType),
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
                  Text(
                    text = "${if (transfer.direction == "SEND") "Outgoing to" else "Incoming from"} ${if (transfer.direction == "SEND") transfer.receiverName else transfer.senderName} • ${formatBytes(transfer.totalBytes)}",
                    fontSize = 11.sp,
                    color = NovaTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = "${transfer.speed} • ${formatTimestamp(transfer.createdAt)}",
                    fontSize = 10.sp,
                    color = NovaTextMuted
                  )
                }
              }

              StatusBadge(status = transfer.status)
            }
          }
        }
      }
    }
  }

  // Inspection Modal
  inspectedTransfer?.let { transfer ->
    TransferDetailsModal(
      transfer = transfer,
      onDismiss = { viewModel.inspectTransfer(null) },
      onDelete = { viewModel.deleteTransfer(transfer.id) }
    )
  }
}
