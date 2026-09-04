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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NovaViewModel
import com.example.ui.components.UserAvatar
import com.example.ui.components.rememberNearDevicePermissionState
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaBorderSubtle
import com.example.ui.theme.NovaPrimary
import com.example.ui.theme.NovaPrimaryLight
import com.example.ui.theme.NovaSuccess
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.util.PermissionHelper

@Composable
fun SettingsScreen(
  viewModel: NovaViewModel,
  modifier: Modifier = Modifier
) {
  var autoAcceptTrusted by remember { mutableStateOf(true) }
  var keepScreenAwake by remember { mutableStateOf(true) }
  var directHotspotMode by remember { mutableStateOf(true) }
  var shaIntegrityVerification by remember { mutableStateOf(true) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(com.example.ui.theme.NovaBackground)
      .testTag("settings_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Identity & Profile
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, com.example.ui.theme.NovaBorderSubtle)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          UserAvatar(
            name = "Mohamed",
            initials = "MO",
            colorHex = 0xFF6750A4,
            size = 54.dp,
            isOnline = true,
            isVerified = true
          )

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Mohamed Ahmed",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = NovaTextPrimary
              )
              Spacer(modifier = Modifier.width(4.dp))
              Icon(Icons.Default.Verified, contentDescription = null, tint = NovaPrimary, modifier = Modifier.size(16.dp))
            }
            Text(
              text = "@moha • Primary Direct Node",
              fontSize = 12.sp,
              color = NovaTextSecondary
            )
            Text(
              text = "Device: Google Pixel 9 Pro",
              fontSize = 11.sp,
              color = NovaTextMuted
            )
          }
        }
      }
    }

    // 2. Transfer Preferences
    item {
      SettingsGroup(title = "TRANSFER PREFERENCES") {
        SettingsToggleRow(
          icon = Icons.Default.Security,
          title = "Auto-Accept from Trusted Devices",
          subtitle = "Bypass manual prompt for verified paired hardware",
          checked = autoAcceptTrusted,
          onCheckedChange = { autoAcceptTrusted = it }
        )

        SettingsToggleRow(
          icon = Icons.Default.Computer,
          title = "Keep Screen Awake",
          subtitle = "Prevent device sleep during large active transfers",
          checked = keepScreenAwake,
          onCheckedChange = { keepScreenAwake = it }
        )

        SettingsToggleRow(
          icon = Icons.Default.Wifi,
          title = "Prefer Wi-Fi Direct (High Speed)",
          subtitle = "Utilize 5GHz/6GHz P2P links over Bluetooth",
          checked = directHotspotMode,
          onCheckedChange = { directHotspotMode = it }
        )

        SettingsNavRow(
          icon = Icons.Default.Folder,
          title = "Download Location",
          value = "/Downloads/NovaSend"
        )
      }
    }

    // 3. Security & Cryptography
    item {
      SettingsGroup(title = "SECURITY & PROTOCOLS") {
        SettingsToggleRow(
          icon = Icons.Default.Lock,
          title = "Mandatory SHA-256 Verification",
          subtitle = "Validate checksum before finalizing received files",
          checked = shaIntegrityVerification,
          onCheckedChange = { shaIntegrityVerification = it }
        )

        SettingsNavRow(
          icon = Icons.Default.Key,
          title = "Encryption Algorithm",
          value = "AES-256-GCM (Hardware Accel)"
        )

        SettingsNavRow(
          icon = Icons.Default.Storage,
          title = "Local Transmission Buffer",
          value = "64 KB Dynamic Slices"
        )
      }
    }

    // 4. Permissions & Hardware Access
    item {
      SettingsGroup(title = "SYSTEM PERMISSIONS & CONNECTIVITY") {
        val permissionState = rememberNearDevicePermissionState()
        val context = LocalContext.current

        // Near-Device Connectivity row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(Icons.Default.Wifi, contentDescription = null, tint = NovaPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text("Near-Device Radios", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NovaTextPrimary)
              Text("Wi-Fi Direct & Bluetooth scanning", fontSize = 11.sp, color = NovaTextSecondary)
            }
          }
          if (permissionState.hasPermissions) {
            Surface(
              color = NovaSuccess.copy(alpha = 0.12f),
              shape = RoundedCornerShape(100.dp)
            ) {
              Text(
                "Granted",
                color = NovaSuccess,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              )
            }
          } else {
            Button(
              onClick = { permissionState.onLaunchPermissionRequest() },
              shape = RoundedCornerShape(100.dp),
              colors = ButtonDefaults.buttonColors(containerColor = NovaPrimary),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
              Text("Grant", fontSize = 11.sp)
            }
          }
        }

        HorizontalDivider(color = NovaBorderSubtle)

        // Storage Access row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(Icons.Default.Folder, contentDescription = null, tint = NovaPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text("Storage & Documents Access", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NovaTextPrimary)
              Text("Scoped SAF & zero-permission Photo Picker", fontSize = 11.sp, color = NovaTextSecondary)
            }
          }
          Surface(
            color = NovaSuccess.copy(alpha = 0.12f),
            shape = RoundedCornerShape(100.dp)
          ) {
            Text(
              "Active (SAF)",
              color = NovaSuccess,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }
        }

        HorizontalDivider(color = NovaBorderSubtle)

        // Settings Redirect row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { PermissionHelper.openAppSettings(context) }
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Settings, contentDescription = null, tint = NovaPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text("Manage in System Settings", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NovaTextPrimary)
              Text("Revoke or customize OS-level privileges", fontSize = 11.sp, color = NovaTextSecondary)
            }
          }
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = NovaTextSecondary, modifier = Modifier.size(16.dp))
        }
      }
    }

    // 5. About NOVA-SEND
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, NovaBorder)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, contentDescription = null, tint = NovaPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("About NOVA-SEND", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NovaTextPrimary)
          }
          Text(
            text = "NOVA-SEND is a high-performance local peer-to-peer file transfer engine designed for secure and fast sharing without external servers.",
            fontSize = 12.sp,
            color = NovaTextSecondary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Version 2.4.0 (Build 4802) • Engine v3.1-p2p",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = NovaTextMuted
          )
        }
      }
    }
  }
}

@Composable
private fun SettingsGroup(
  title: String,
  content: @Composable () -> Unit
) {
  Column {
    Text(
      text = title,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      color = NovaTextSecondary,
      letterSpacing = 1.sp,
      modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
    )
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      border = BorderStroke(1.dp, com.example.ui.theme.NovaBorderSubtle)
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        content()
      }
    }
  }
}

@Composable
private fun SettingsToggleRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
      Box(
        modifier = Modifier
          .size(34.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(NovaPrimaryLight),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = icon, contentDescription = null, tint = NovaPrimary, modifier = Modifier.size(18.dp))
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = NovaTextPrimary)
        Text(text = subtitle, fontSize = 11.sp, color = NovaTextSecondary)
      }
    }

    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = NovaPrimary,
        uncheckedThumbColor = Color.White,
        uncheckedTrackColor = Color(0xFFCBD5E1)
      )
    )
  }
}

@Composable
private fun SettingsNavRow(
  icon: ImageVector,
  title: String,
  value: String
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
      Box(
        modifier = Modifier
          .size(34.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(NovaPrimaryLight),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = icon, contentDescription = null, tint = NovaPrimary, modifier = Modifier.size(18.dp))
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column {
        Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = NovaTextPrimary)
        Text(text = value, fontSize = 11.sp, color = NovaTextMuted)
      }
    }

    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = NovaTextMuted, modifier = Modifier.size(20.dp))
  }
}
