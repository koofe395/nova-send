package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.engine.EngineState
import com.example.ui.components.NotificationsDialog
import com.example.ui.screens.DevicesScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.MessagesScreen
import com.example.ui.screens.OverviewScreen
import com.example.ui.screens.QrCodeDialog
import com.example.ui.screens.ReceiveScreen
import com.example.ui.screens.SendScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransfersScreen
import com.example.ui.theme.NovaAccent
import com.example.ui.theme.NovaBackground
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaBorderSubtle
import com.example.ui.theme.NovaNavPill
import com.example.ui.theme.NovaOnPrimaryContainer
import com.example.ui.theme.NovaPrimary
import com.example.ui.theme.NovaPrimaryLight
import com.example.ui.theme.NovaSuccess
import com.example.ui.theme.NovaSurfaceVariant
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary

enum class NovaNavDestination(
  val label: String,
  val icon: ImageVector,
  val testTag: String
) {
  OVERVIEW("Overview", Icons.Default.Dashboard, "nav_overview"),
  SEND("Send", Icons.Default.Upload, "nav_send"),
  RECEIVE("Receive", Icons.Default.Download, "nav_receive"),
  TRANSFERS("Transfers", Icons.Default.Sync, "nav_transfers"),
  MESSAGES("Messages", Icons.Default.Chat, "nav_messages"),
  DEVICES("Devices", Icons.Default.Devices, "nav_devices"),
  HISTORY("History", Icons.Default.History, "nav_history"),
  SETTINGS("Settings", Icons.Default.Settings, "nav_settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaApp(
  viewModel: NovaViewModel = viewModel(),
  initialDestination: NovaNavDestination = NovaNavDestination.OVERVIEW,
  openQrOnLaunch: Boolean = false
) {
  var currentDestination by remember { mutableStateOf(initialDestination) }
  var showNotificationsDialog by remember { mutableStateOf(false) }
  var showQrDialog by remember { mutableStateOf(openQrOnLaunch) }

  val unreadCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()
  val allNotifications by viewModel.allNotifications.collectAsStateWithLifecycle()
  val incomingTransfers by viewModel.incomingTransfers.collectAsStateWithLifecycle()
  val activeProgress by viewModel.activeProgress.collectAsStateWithLifecycle()

  Scaffold(
    containerColor = NovaBackground,
    topBar = {
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = NovaBackground,
          titleContentColor = NovaTextPrimary
        ),
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(NovaPrimary),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "NOVA-SEND",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = NovaTextPrimary,
                letterSpacing = 0.5.sp
              )
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(NovaSuccess)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "P2P Network Online",
                  fontSize = 11.sp,
                  color = NovaTextMuted
                )
              }
            }
          }
        },
        actions = {
          // QR Code Scanner & Pair Button
          IconButton(
            onClick = { showQrDialog = true },
            modifier = Modifier.testTag("top_nav_qr_btn")
          ) {
            Icon(
              imageVector = Icons.Default.QrCodeScanner,
              contentDescription = "QR Code Discovery & Pairing",
              tint = if (showQrDialog) NovaPrimary else NovaTextSecondary
            )
          }

          // History Button
          IconButton(
            onClick = { currentDestination = NovaNavDestination.HISTORY },
            modifier = Modifier.testTag("top_nav_history_btn")
          ) {
            Icon(
              imageVector = Icons.Default.History,
              contentDescription = "History",
              tint = if (currentDestination == NovaNavDestination.HISTORY) NovaPrimary else NovaTextSecondary
            )
          }

          // Devices Button
          IconButton(
            onClick = { currentDestination = NovaNavDestination.DEVICES },
            modifier = Modifier.testTag("top_nav_devices_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Devices,
              contentDescription = "Devices",
              tint = if (currentDestination == NovaNavDestination.DEVICES) NovaPrimary else NovaTextSecondary
            )
          }

          // Settings Button
          IconButton(
            onClick = { currentDestination = NovaNavDestination.SETTINGS },
            modifier = Modifier.testTag("top_nav_settings_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = "Settings",
              tint = if (currentDestination == NovaNavDestination.SETTINGS) NovaPrimary else NovaTextSecondary
            )
          }

          // Notifications Button with badge
          IconButton(
            onClick = { showNotificationsDialog = true },
            modifier = Modifier.testTag("top_nav_notifications_btn")
          ) {
            BadgedBox(
              badge = {
                if (unreadCount > 0) {
                  Badge(containerColor = NovaPrimary) {
                    Text("$unreadCount", color = Color.White)
                  }
                }
              }
            ) {
              Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = NovaTextSecondary
              )
            }
          }

          Spacer(modifier = Modifier.width(4.dp))

          // Geometric Balance profile avatar icon button
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(NovaPrimaryLight),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = "User Profile",
              tint = com.example.ui.theme.NovaOnPrimaryContainer,
              modifier = Modifier.size(22.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
        }
      )
    },
    bottomBar = {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(NovaBackground)
          .padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        NavigationBar(
          containerColor = NovaSurfaceVariant,
          tonalElevation = 0.dp,
          modifier = Modifier
            .clip(RoundedCornerShape(28.dp))
            .border(1.dp, NovaBorderSubtle, RoundedCornerShape(28.dp))
        ) {
          listOf(
            NovaNavDestination.OVERVIEW,
            NovaNavDestination.SEND,
            NovaNavDestination.RECEIVE,
            NovaNavDestination.TRANSFERS,
            NovaNavDestination.MESSAGES
          ).forEach { dest ->
            val isSelected = currentDestination == dest
            val badgeCount = when (dest) {
              NovaNavDestination.RECEIVE -> incomingTransfers.size
              NovaNavDestination.TRANSFERS -> if (activeProgress.state in listOf(EngineState.TRANSFERRING, EngineState.CONNECTING)) 1 else 0
              else -> 0
            }

            NavigationBarItem(
              selected = isSelected,
              onClick = { currentDestination = dest },
              icon = {
                BadgedBox(
                  badge = {
                    if (badgeCount > 0) {
                      Badge(containerColor = NovaPrimary) {
                        Text("$badgeCount", color = Color.White)
                      }
                    }
                  }
                ) {
                  Icon(
                    imageVector = dest.icon,
                    contentDescription = dest.label
                  )
                }
              },
              label = {
                Text(
                  text = dest.label,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NovaTextPrimary,
                selectedTextColor = NovaTextPrimary,
                indicatorColor = com.example.ui.theme.NovaNavPill,
                unselectedIconColor = NovaTextSecondary,
                unselectedTextColor = NovaTextSecondary
              ),
              modifier = Modifier.testTag(dest.testTag)
            )
          }
        }
      }
    },
    floatingActionButton = {
      if (currentDestination == NovaNavDestination.OVERVIEW) {
        FloatingActionButton(
          onClick = { currentDestination = NovaNavDestination.SEND },
          shape = RoundedCornerShape(16.dp),
          containerColor = NovaAccent,
          contentColor = com.example.ui.theme.NovaOnPrimaryContainer,
          modifier = Modifier.testTag("fab_send_now")
        ) {
          Icon(Icons.Default.Upload, contentDescription = "Send File", modifier = Modifier.size(26.dp))
        }
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      when (currentDestination) {
        NovaNavDestination.OVERVIEW -> OverviewScreen(
          viewModel = viewModel,
          onNavigateToSend = { currentDestination = NovaNavDestination.SEND },
          onNavigateToReceive = { currentDestination = NovaNavDestination.RECEIVE },
          onNavigateToDevices = { currentDestination = NovaNavDestination.DEVICES },
          onNavigateToMessages = { currentDestination = NovaNavDestination.MESSAGES },
          onOpenQrDialog = { showQrDialog = true }
        )
        NovaNavDestination.SEND -> SendScreen(
          viewModel = viewModel,
          onFinishFlow = { currentDestination = NovaNavDestination.OVERVIEW }
        )
        NovaNavDestination.RECEIVE -> ReceiveScreen(
          viewModel = viewModel
        )
        NovaNavDestination.TRANSFERS -> TransfersScreen(
          viewModel = viewModel
        )
        NovaNavDestination.MESSAGES -> MessagesScreen(
          viewModel = viewModel
        )
        NovaNavDestination.DEVICES -> DevicesScreen(
          viewModel = viewModel
        )
        NovaNavDestination.HISTORY -> HistoryScreen(
          viewModel = viewModel
        )
        NovaNavDestination.SETTINGS -> SettingsScreen(
          viewModel = viewModel
        )
      }
    }
  }

  // Notifications Dialog
  if (showNotificationsDialog) {
    NotificationsDialog(
      notifications = allNotifications,
      onDismiss = { showNotificationsDialog = false },
      onMarkAllRead = { viewModel.markNotificationsRead() },
      onClearAll = { viewModel.clearAllNotifications() }
    )
  }

  // QR Code Discovery & Fast Pairing Dialog
  if (showQrDialog) {
    QrCodeDialog(
      viewModel = viewModel,
      onDismiss = { showQrDialog = false },
      onDevicePairedForSend = {
        currentDestination = NovaNavDestination.SEND
      }
    )
  }
}
