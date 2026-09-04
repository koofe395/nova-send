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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DeviceEntity
import com.example.ui.NovaViewModel
import com.example.ui.components.NearDevicePermissionBanner
import com.example.ui.components.getDeviceIcon
import com.example.ui.components.rememberNearDevicePermissionState
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaPrimary
import com.example.ui.theme.NovaPrimaryLight
import com.example.ui.theme.NovaSuccess
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary

@Composable
fun DevicesScreen(
  viewModel: NovaViewModel,
  modifier: Modifier = Modifier
) {
  val devices by viewModel.allDevices.collectAsStateWithLifecycle()
  var showPairDialog by remember { mutableStateOf(false) }
  var showSearchingScreen by remember { mutableStateOf(false) }
  val permissionState = rememberNearDevicePermissionState()

  if (showSearchingScreen) {
    SearchingDevicesScreen(
      onDeviceSelected = { scanned ->
        val devType = when {
          scanned.platform.contains("mac", ignoreCase = true) || scanned.platform.contains("windows", ignoreCase = true) -> "LAPTOP"
          scanned.platform.contains("tablet", ignoreCase = true) || scanned.platform.contains("ipad", ignoreCase = true) -> "TABLET"
          else -> "PHONE"
        }
        viewModel.pairNewDevice(
          name = scanned.name,
          type = devType,
          platform = scanned.platform
        )
        showSearchingScreen = false
      },
      onBack = { showSearchingScreen = false }
    )
    return
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(com.example.ui.theme.NovaBackground)
      .testTag("devices_screen")
  ) {
    // Header
    Surface(
      color = Color.White,
      border = BorderStroke(1.dp, com.example.ui.theme.NovaBorderSubtle),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Paired & Trusted Hardware",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = NovaTextPrimary
          )
          Text(
            text = "${devices.count { it.isOnline }} online • ${devices.count { it.isTrusted }} trusted nodes",
            fontSize = 12.sp,
            color = NovaTextSecondary
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedButton(
            onClick = { showSearchingScreen = true },
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, NovaPrimary),
            modifier = Modifier.testTag("scan_devices_radar_btn")
          ) {
            Icon(Icons.Default.Radar, contentDescription = null, tint = NovaPrimary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Radar Scan", fontSize = 12.sp, color = NovaPrimary, fontWeight = FontWeight.SemiBold)
          }

          Button(
            onClick = { showPairDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = NovaPrimary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.testTag("pair_new_device_btn")
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Pair", fontSize = 12.sp)
          }
        }
      }
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Near Device Permission Banner
      item {
        NearDevicePermissionBanner(permissionState = permissionState)
      }

      items(devices, key = { it.id }) { device ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.dp, com.example.ui.theme.NovaBorderSubtle)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                  modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (device.isOnline) NovaPrimaryLight else Color(0xFFF1F5F9)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = getDeviceIcon(device.type),
                    contentDescription = null,
                    tint = if (device.isOnline) NovaPrimary else NovaTextMuted,
                    modifier = Modifier.size(24.dp)
                  )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = device.name,
                      fontWeight = FontWeight.Bold,
                      fontSize = 14.sp,
                      color = NovaTextPrimary
                    )
                    if (device.isTrusted) {
                      Spacer(modifier = Modifier.width(4.dp))
                      Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Trusted",
                        tint = NovaSuccess,
                        modifier = Modifier.size(14.dp)
                      )
                    }
                  }
                  Text(
                    text = "${device.platform} • ${if (device.isOnline) "Active now" else device.lastSeen}",
                    fontSize = 12.sp,
                    color = if (device.isOnline) NovaSuccess else NovaTextMuted
                  )
                  Text(
                    text = device.connectionType,
                    fontSize = 11.sp,
                    color = NovaTextSecondary
                  )
                }
              }

              // Online toggle
              IconButton(
                onClick = { viewModel.toggleDeviceOnline(device) },
                modifier = Modifier.size(36.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.PowerSettingsNew,
                  contentDescription = "Toggle Online",
                  tint = if (device.isOnline) NovaSuccess else NovaTextMuted
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Trust Switch Row
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF8FAFC))
                .padding(horizontal = 12.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Auto-Accept Transfers (Trusted)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = NovaTextPrimary
              )
              Switch(
                checked = device.isTrusted,
                onCheckedChange = { viewModel.toggleDeviceTrust(device) },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = NovaPrimary,
                  uncheckedThumbColor = Color.White,
                  uncheckedTrackColor = Color(0xFFCBD5E1)
                )
              )
            }
          }
        }
      }
    }
  }

  if (showPairDialog) {
    PairDeviceDialog(
      onDismiss = { showPairDialog = false },
      onPair = { name, type, platform ->
        viewModel.pairNewDevice(name, type, platform)
        showPairDialog = false
      }
    )
  }
}

@Composable
private fun PairDeviceDialog(
  onDismiss: () -> Unit,
  onPair: (name: String, type: String, platform: String) -> Unit
) {
  var deviceName by remember { mutableStateOf("") }
  var deviceType by remember { mutableStateOf("LAPTOP") }
  var platform by remember { mutableStateOf("macOS") }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = Color.White,
      border = BorderStroke(1.dp, NovaBorder),
      modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Text("Pair New Hardware Node", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = NovaTextPrimary)
        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = deviceName,
          onValueChange = { deviceName = it },
          label = { Text("Device Name (e.g. Work PC)") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Form Factor:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NovaTextPrimary)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf("LAPTOP", "DESKTOP", "TABLET", "PHONE").forEach { t ->
            val isSelected = deviceType == t
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) NovaPrimary else Color(0xFFF1F5F9))
                .clickable { deviceType = t }
                .padding(vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = t.take(4),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else NovaTextSecondary
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
              if (deviceName.isNotBlank()) {
                onPair(deviceName, deviceType, platform)
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = NovaPrimary),
            modifier = Modifier.weight(1f),
            enabled = deviceName.isNotBlank()
          ) {
            Text("Pair Node")
          }
        }
      }
    }
  }
}
