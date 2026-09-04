package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.EngineState
import com.example.ui.NovaViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.components.UserAvatar
import com.example.ui.components.formatBytes
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaError
import com.example.ui.theme.NovaPrimary
import com.example.ui.theme.NovaPrimaryLight
import com.example.ui.theme.NovaSuccess
import com.example.ui.theme.NovaSuccessBg
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary

@Composable
fun ReceiveScreen(
  viewModel: NovaViewModel,
  modifier: Modifier = Modifier
) {
  val incomingTransfers by viewModel.incomingTransfers.collectAsStateWithLifecycle()
  val activeProgress by viewModel.activeProgress.collectAsStateWithLifecycle()

  var visibilityScope by remember { mutableStateOf("CONTACTS_ONLY") }

  val infiniteTransition = rememberInfiniteTransition(label = "radar_pulse")
  val radarAlpha by infiniteTransition.animateFloat(
    initialValue = 0.2f,
    targetValue = 0.9f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200),
      repeatMode = RepeatMode.Reverse
    ),
    label = "radar_alpha"
  )

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(com.example.ui.theme.NovaBackground)
      .testTag("receive_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Header & Radar Status Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, com.example.ui.theme.NovaBorderSubtle)
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(NovaPrimaryLight),
            contentAlignment = Alignment.Center
          ) {
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(NovaPrimary.copy(alpha = radarAlpha * 0.3f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Radar,
                contentDescription = null,
                tint = NovaPrimary,
                modifier = Modifier.size(28.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "Receiver Node Active",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = NovaTextPrimary
          )
          Text(
            text = "Visible as: Mohamed's Pixel 9 Pro",
            fontSize = 13.sp,
            color = NovaTextSecondary
          )
          Text(
            text = "Local Wi-Fi Direct • BLE Discovery • AES-256",
            fontSize = 11.sp,
            color = NovaTextMuted
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Visibility Scope Selector
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf(
              Pair("EVERYONE", "Everyone"),
              Pair("CONTACTS_ONLY", "Contacts Only"),
              Pair("TRUSTED_ONLY", "Trusted Only")
            ).forEach { (key, label) ->
              val isSelected = visibilityScope == key
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSelected) NovaPrimary else Color(0xFFF1F5F9))
                  .clickable { visibilityScope = key }
                  .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = label,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) Color.White else NovaTextSecondary,
                  textAlign = TextAlign.Center
                )
              }
            }
          }
        }
      }
    }

    // 2. Active Receiving Monitor (if currently receiving)
    if (activeProgress.direction == "RECEIVE" && activeProgress.state != EngineState.IDLE) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.5.dp, NovaPrimary)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "Incoming Data Stream",
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = NovaTextPrimary
                )
                Text(
                  text = "From ${activeProgress.partnerName} (${activeProgress.partnerDevice})",
                  fontSize = 12.sp,
                  color = NovaTextSecondary
                )
              }
              StatusBadge(status = activeProgress.state.name)
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
              progress = { activeProgress.overallProgress },
              modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
              color = if (activeProgress.state == EngineState.COMPLETED) NovaSuccess else NovaPrimary,
              trackColor = NovaPrimaryLight,
              strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "${formatBytes(activeProgress.transferredBytes)} / ${formatBytes(activeProgress.totalBytes)} (${(activeProgress.overallProgress * 100).toInt()}%)",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = NovaTextPrimary
              )
              Text(
                text = if (activeProgress.speedMbPerSec > 0) "${"%.1f".format(activeProgress.speedMbPerSec)} MB/s" else activeProgress.statusMessage,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = NovaPrimary
              )
            }

            if (activeProgress.state == EngineState.COMPLETED) {
              Spacer(modifier = Modifier.height(14.dp))
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(NovaSuccessBg)
                  .padding(10.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Folder, contentDescription = null, tint = NovaSuccess, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Saved to /Downloads/NovaSend",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NovaSuccess
                  )
                }
              }
            }
          }
        }
      }
    }

    // 3. Incoming Transfer Requests Header & Action
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Pending Incoming Requests (${incomingTransfers.size})",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = NovaTextPrimary
        )

        OutlinedButton(
          onClick = { viewModel.triggerSimulateIncoming() },
          shape = RoundedCornerShape(8.dp),
          border = BorderStroke(1.dp, NovaBorder),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          modifier = Modifier.height(32.dp).testTag("simulate_incoming_btn")
        ) {
          Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Simulate Offer", fontSize = 11.sp)
        }
      }
    }

    // 4. List of Incoming Offers
    if (incomingTransfers.isEmpty()) {
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
              .padding(32.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.Download,
                contentDescription = null,
                tint = NovaTextMuted,
                modifier = Modifier.size(36.dp)
              )
              Spacer(modifier = Modifier.height(10.dp))
              Text("No incoming transfer requests", fontWeight = FontWeight.Bold, color = NovaTextPrimary)
              Text(
                "When someone nearby sends files, they will appear here for review.",
                fontSize = 12.sp,
                color = NovaTextSecondary,
                textAlign = TextAlign.Center
              )
            }
          }
        }
      }
    } else {
      items(incomingTransfers, key = { it.id }) { incoming ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("incoming_item_${incoming.id}"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.5.dp, Color(0xFF6366F1))
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
                  size = 44.dp,
                  isVerified = true,
                  isOnline = true
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = incoming.senderName,
                      fontWeight = FontWeight.Bold,
                      fontSize = 15.sp,
                      color = NovaTextPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                      imageVector = Icons.Default.Verified,
                      contentDescription = "Verified",
                      tint = NovaPrimary,
                      modifier = Modifier.size(14.dp)
                    )
                  }
                  Text(
                    text = "${incoming.senderDevice} • @${incoming.senderUsername}",
                    fontSize = 12.sp,
                    color = NovaTextMuted
                  )
                }
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(NovaPrimaryLight)
                  .padding(horizontal = 10.dp, vertical = 5.dp)
              ) {
                Text(
                  text = formatBytes(incoming.totalBytes),
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = NovaPrimary
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, NovaBorder, RoundedCornerShape(10.dp))
                .padding(12.dp)
            ) {
              Column {
                Text(
                  text = "Offered Payload:",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = NovaTextMuted
                )
                Text(
                  text = incoming.filesSummary,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Medium,
                  color = NovaTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Lock, contentDescription = null, tint = NovaSuccess, modifier = Modifier.size(12.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = incoming.securityGrade,
                    fontSize = 11.sp,
                    color = NovaSuccess
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              OutlinedButton(
                onClick = { viewModel.declineIncoming(incoming) },
                modifier = Modifier.weight(1f).height(42.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, NovaBorder)
              ) {
                Icon(Icons.Default.Close, contentDescription = null, tint = NovaError, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Decline", fontSize = 12.sp, color = NovaError)
              }

              Button(
                onClick = { viewModel.acceptIncoming(incoming) },
                colors = ButtonDefaults.buttonColors(containerColor = NovaPrimary),
                modifier = Modifier.weight(1.3f).height(42.dp).testTag("accept_button_${incoming.id}"),
                shape = RoundedCornerShape(10.dp)
              ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Accept & Receive", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }
}
