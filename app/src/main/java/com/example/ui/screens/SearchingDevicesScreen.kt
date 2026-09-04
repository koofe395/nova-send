package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ContactEntity
import com.example.data.model.DeviceEntity
import com.example.ui.components.NearDevicePermissionBanner
import com.example.ui.components.UserAvatar
import com.example.ui.components.rememberNearDevicePermissionState
import com.example.ui.theme.NovaAccent
import com.example.ui.theme.NovaBackground
import com.example.ui.theme.NovaBorderSubtle
import com.example.ui.theme.NovaPrimary
import com.example.ui.theme.NovaPrimaryLight
import com.example.ui.theme.NovaSuccess
import com.example.ui.theme.NovaSurfaceVariant
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import kotlinx.coroutines.delay

data class ScannedNearbyDevice(
  val id: String,
  val name: String,
  val owner: String,
  val platform: String,
  val protocol: String,
  val signalDbm: Int,
  val distanceMeters: Float,
  val isTrusted: Boolean,
  val avatarInitials: String,
  val avatarColorHex: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchingDevicesScreen(
  modifier: Modifier = Modifier,
  onDeviceSelected: (ScannedNearbyDevice) -> Unit,
  onBack: () -> Unit
) {
  val permissionState = rememberNearDevicePermissionState()

  var isScanning by remember { mutableStateOf(true) }
  var selectedProtocolFilter by remember { mutableStateOf("ALL") }
  var showManualIpDialog by remember { mutableStateOf(false) }
  var showTroubleshooting by remember { mutableStateOf(false) }
  var scanWaveStage by remember { mutableStateOf(0) }

  // Scanned mock devices that populate progressively during scan
  val baseDevices = remember {
    listOf(
      ScannedNearbyDevice(
        id = "DEV-M1",
        name = "Mohamed's MacBook Pro 16\"",
        owner = "Mohamed A.",
        platform = "macOS",
        protocol = "Wi-Fi Direct 5GHz",
        signalDbm = -38,
        distanceMeters = 0.8f,
        isTrusted = true,
        avatarInitials = "MB",
        avatarColorHex = 0xFF4F46E5
      ),
      ScannedNearbyDevice(
        id = "DEV-W1",
        name = "Studio-Desk-PC (RTX 4090)",
        owner = "Studio Rig",
        platform = "Windows 11",
        protocol = "LAN Gigabit",
        signalDbm = -45,
        distanceMeters = 2.4f,
        isTrusted = true,
        avatarInitials = "PC",
        avatarColorHex = 0xFF0284C7
      ),
      ScannedNearbyDevice(
        id = "DEV-A1",
        name = "Pixel 8 Pro (Sarah)",
        owner = "Sarah Jenkins",
        platform = "Android 14",
        protocol = "Wi-Fi Direct P2P",
        signalDbm = -58,
        distanceMeters = 3.2f,
        isTrusted = false,
        avatarInitials = "SJ",
        avatarColorHex = 0xFF10B981
      ),
      ScannedNearbyDevice(
        id = "DEV-I1",
        name = "iPad Pro M2 (Design)",
        owner = "Alex Rivera",
        platform = "iPadOS",
        protocol = "Bluetooth LE 5.2",
        signalDbm = -72,
        distanceMeters = 5.6f,
        isTrusted = false,
        avatarInitials = "AR",
        avatarColorHex = 0xFFF59E0B
      )
    )
  }

  // Animate progressive discovery simulation
  var visibleDevicesCount by remember { mutableStateOf(1) }
  LaunchedEffect(isScanning) {
    if (isScanning) {
      visibleDevicesCount = 1
      delay(700)
      visibleDevicesCount = 2
      delay(1200)
      visibleDevicesCount = 3
      delay(1500)
      visibleDevicesCount = 4
    }
  }

  val activeDevices = remember(visibleDevicesCount, selectedProtocolFilter) {
    baseDevices.take(visibleDevicesCount).filter { dev ->
      when (selectedProtocolFilter) {
        "WIFI" -> dev.protocol.contains("Wi-Fi", ignoreCase = true)
        "BLE" -> dev.protocol.contains("Bluetooth", ignoreCase = true)
        "LAN" -> dev.protocol.contains("LAN", ignoreCase = true)
        else -> true
      }
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(NovaBackground)
      .testTag("searching_devices_screen")
  ) {
    // Top Bar
    Surface(
      color = Color.White,
      border = BorderStroke(1.dp, NovaBorderSubtle),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("search_devices_back_btn")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NovaTextPrimary)
          }
          Spacer(modifier = Modifier.width(4.dp))
          Column {
            Text(
              text = "Searching for Devices",
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              color = NovaTextPrimary
            )
            Text(
              text = if (isScanning) "Active 5GHz & BLE scanning..." else "Scan paused",
              fontSize = 11.sp,
              color = if (isScanning) NovaPrimary else NovaTextMuted
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = { isScanning = !isScanning },
            modifier = Modifier.testTag("toggle_scan_btn")
          ) {
            Icon(
              imageVector = if (isScanning) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = if (isScanning) "Pause Scan" else "Resume Scan",
              tint = NovaPrimary
            )
          }

          IconButton(
            onClick = { showManualIpDialog = true },
            modifier = Modifier.testTag("manual_pair_ip_btn")
          ) {
            Icon(Icons.Default.QrCode, contentDescription = "Manual IP / PIN", tint = NovaTextSecondary)
          }
        }
      }
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .weight(1f),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Permission Banner
      item {
        NearDevicePermissionBanner(permissionState = permissionState)
      }

      // Radar Visual Feedback Component
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("radar_visual_card"),
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.dp, NovaBorderSubtle)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            // Animated Radar View
            RadarScannerVisual(
              isScanning = isScanning,
              discoveredCount = activeDevices.size,
              modifier = Modifier
                .size(240.dp)
                .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Scan Telemetry Badges
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceEvenly,
              verticalAlignment = Alignment.CenterVertically
            ) {
              ScanTelemetryPill(
                icon = Icons.Default.Wifi,
                title = "Wi-Fi Direct",
                status = "5.0 GHz Ready",
                isActive = isScanning
              )
              ScanTelemetryPill(
                icon = Icons.Default.Bluetooth,
                title = "BLE 5.2",
                status = "Beacon RX/TX",
                isActive = isScanning
              )
              ScanTelemetryPill(
                icon = Icons.Default.Security,
                title = "E2EE TLS",
                status = "ChaCha20",
                isActive = true
              )
            }
          }
        }
      }

      // Protocol Filters
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Discovered Devices (${activeDevices.size})",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = NovaTextPrimary
          )

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ProtocolFilterPill("All", "ALL", selectedProtocolFilter) { selectedProtocolFilter = it }
            ProtocolFilterPill("Wi-Fi", "WIFI", selectedProtocolFilter) { selectedProtocolFilter = it }
            ProtocolFilterPill("BLE", "BLE", selectedProtocolFilter) { selectedProtocolFilter = it }
          }
        }
      }

      // Devices List
      if (activeDevices.isEmpty()) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, NovaBorderSubtle)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(Icons.Default.Refresh, contentDescription = null, tint = NovaPrimary, modifier = Modifier.size(36.dp))
              Spacer(modifier = Modifier.height(10.dp))
              Text("Listening for nearby devices...", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NovaTextPrimary)
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                "Ensure nearby computers, phones, or tablets have NOVA-SEND open on the same Wi-Fi network or Bluetooth range.",
                fontSize = 12.sp,
                color = NovaTextSecondary,
                textAlign = TextAlign.Center
              )
            }
          }
        }
      } else {
        items(activeDevices, key = { it.id }) { device ->
          DiscoveredDeviceCard(
            device = device,
            onClick = { onDeviceSelected(device) }
          )
        }
      }

      // Troubleshooting Section Toggle
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { showTroubleshooting = !showTroubleshooting }
            .testTag("troubleshoot_card"),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = NovaSurfaceVariant),
          border = BorderStroke(1.dp, NovaBorderSubtle)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.HelpOutline, contentDescription = null, tint = NovaPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = "Can't find your recipient device?",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = NovaTextPrimary
                )
              }
              Text(
                text = if (showTroubleshooting) "Hide tips ▲" else "Show tips ▼",
                fontSize = 11.sp,
                color = NovaPrimary,
                fontWeight = FontWeight.Medium
              )
            }

            AnimatedVisibility(visible = showTroubleshooting) {
              Column(
                modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                TroubleshootItem(
                  number = "1",
                  title = "Keep NOVA-SEND open",
                  desc = "Both sender and recipient devices must have the app foregrounded."
                )
                TroubleshootItem(
                  number = "2",
                  title = "Check Wi-Fi or Hotspot",
                  desc = "Connect to the same Wi-Fi router, or enable Wi-Fi Direct on Android."
                )
                TroubleshootItem(
                  number = "3",
                  title = "Toggle Bluetooth",
                  desc = "Bluetooth Low Energy enables rapid beaconing within 10 meters."
                )
                TroubleshootItem(
                  number = "4",
                  title = "Use Manual IP Connect",
                  desc = "Tap the QR/PIN button in the top right to pair directly via local IP."
                )
              }
            }
          }
        }
      }
    }
  }

  // Manual IP / PIN Pairing Dialog
  if (showManualIpDialog) {
    ManualConnectDialog(
      onDismiss = { showManualIpDialog = false },
      onConnect = { ip, pin ->
        showManualIpDialog = false
        val manualDev = ScannedNearbyDevice(
          id = "DEV-MANUAL-$ip",
          name = "Direct Host ($ip)",
          owner = "Manual Peer",
          platform = "Network Device",
          protocol = "TCP Direct Socket",
          signalDbm = -30,
          distanceMeters = 1.0f,
          isTrusted = true,
          avatarInitials = "IP",
          avatarColorHex = 0xFF6750A4
        )
        onDeviceSelected(manualDev)
      }
    )
  }
}

/**
 * Animated Canvas Radar Scanner providing real-time visual feedback.
 */
@Composable
fun RadarScannerVisual(
  isScanning: Boolean,
  discoveredCount: Int,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "RadarTransition")

  // Radar sweep beam rotation
  val sweepAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 3500, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "RadarSweep"
  )

  // Concentric ripple wave 1
  val ripple1 by infiniteTransition.animateFloat(
    initialValue = 0.1f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "Ripple1"
  )

  // Concentric ripple wave 2
  val ripple2 by infiniteTransition.animateFloat(
    initialValue = 0.1f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2400, delayMillis = 800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "Ripple2"
  )

  // Central pulse
  val centerGlow by infiniteTransition.animateFloat(
    initialValue = 0.7f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "CenterGlow"
  )

  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val center = Offset(size.width / 2f, size.height / 2f)
      val maxRadius = size.minDimension / 2f

      // Concentric background rings
      val ringFractions = listOf(0.25f, 0.5f, 0.75f, 1.0f)
      for (frac in ringFractions) {
        drawCircle(
          color = Color(0xFFEADDFF).copy(alpha = 0.45f),
          radius = maxRadius * frac,
          center = center,
          style = Stroke(width = 1.5f)
        )
      }

      // Crosshair lines
      drawLine(
        color = Color(0xFFEADDFF).copy(alpha = 0.35f),
        start = Offset(center.x - maxRadius, center.y),
        end = Offset(center.x + maxRadius, center.y),
        strokeWidth = 1f
      )
      drawLine(
        color = Color(0xFFEADDFF).copy(alpha = 0.35f),
        start = Offset(center.x, center.y - maxRadius),
        end = Offset(center.x, center.y + maxRadius),
        strokeWidth = 1f
      )

      if (isScanning) {
        // Expanding Ripple 1
        val radius1 = maxRadius * ripple1
        val alpha1 = (1f - ripple1).coerceIn(0f, 1f) * 0.45f
        drawCircle(
          color = NovaPrimary.copy(alpha = alpha1),
          radius = radius1,
          center = center,
          style = Stroke(width = 2.5f)
        )

        // Expanding Ripple 2
        val radius2 = maxRadius * ripple2
        val alpha2 = (1f - ripple2).coerceIn(0f, 1f) * 0.45f
        drawCircle(
          color = NovaAccent.copy(alpha = alpha2),
          radius = radius2,
          center = center,
          style = Stroke(width = 2f)
        )

        // Sweeping Radar Arc
        val sweepBrush = Brush.sweepGradient(
          colors = listOf(
            Color.Transparent,
            Color.Transparent,
            NovaPrimary.copy(alpha = 0.05f),
            NovaPrimary.copy(alpha = 0.25f)
          ),
          center = center
        )

        drawArc(
          brush = sweepBrush,
          startAngle = sweepAngle - 50f,
          sweepAngle = 50f,
          useCenter = true,
          topLeft = Offset(center.x - maxRadius, center.y - maxRadius),
          size = androidx.compose.ui.geometry.Size(maxRadius * 2, maxRadius * 2)
        )

        // Sweep leading line
        val radians = Math.toRadians(sweepAngle.toDouble())
        val endX = (center.x + maxRadius * Math.cos(radians)).toFloat()
        val endY = (center.y + maxRadius * Math.sin(radians)).toFloat()
        drawLine(
          color = NovaPrimary.copy(alpha = 0.7f),
          start = center,
          end = Offset(endX, endY),
          strokeWidth = 2f,
          cap = StrokeCap.Round
        )
      }

      // Simulated detected blips on the radar
      if (discoveredCount >= 1) {
        drawCircle(
          color = NovaPrimary,
          radius = 5.dp.toPx(),
          center = Offset(center.x + maxRadius * 0.42f, center.y - maxRadius * 0.35f)
        )
      }
      if (discoveredCount >= 2) {
        drawCircle(
          color = Color(0xFF0284C7),
          radius = 5.dp.toPx(),
          center = Offset(center.x - maxRadius * 0.55f, center.y - maxRadius * 0.22f)
        )
      }
      if (discoveredCount >= 3) {
        drawCircle(
          color = NovaSuccess,
          radius = 5.dp.toPx(),
          center = Offset(center.x + maxRadius * 0.65f, center.y + maxRadius * 0.45f)
        )
      }
      if (discoveredCount >= 4) {
        drawCircle(
          color = Color(0xFFF59E0B),
          radius = 5.dp.toPx(),
          center = Offset(center.x - maxRadius * 0.35f, center.y + maxRadius * 0.68f)
        )
      }
    }

    // Central pulsing device icon
    Box(
      modifier = Modifier
        .size(46.dp)
        .clip(CircleShape)
        .background(NovaPrimary)
        .border(2.dp, Color.White, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.PhoneAndroid,
        contentDescription = "My Device",
        tint = Color.White,
        modifier = Modifier.size(22.dp)
      )
    }
  }
}

@Composable
private fun ScanTelemetryPill(
  icon: ImageVector,
  title: String,
  status: String,
  isActive: Boolean
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.padding(horizontal = 4.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isActive) NovaPrimary else NovaTextMuted,
        modifier = Modifier.size(14.dp)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = NovaTextPrimary
      )
    }
    Text(
      text = status,
      fontSize = 10.sp,
      color = if (isActive) NovaSuccess else NovaTextMuted
    )
  }
}

@Composable
private fun ProtocolFilterPill(
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
private fun DiscoveredDeviceCard(
  device: ScannedNearbyDevice,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("scanned_device_${device.id}"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = BorderStroke(1.dp, NovaBorderSubtle)
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
          name = device.name,
          initials = device.avatarInitials,
          colorHex = device.avatarColorHex,
          size = 46.dp,
          isOnline = true,
          isVerified = device.isTrusted
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = device.name,
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold,
              color = NovaTextPrimary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            if (device.isTrusted) {
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = "Trusted",
                tint = NovaPrimary,
                modifier = Modifier.size(14.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(2.dp))

          Text(
            text = "${device.owner} • ${device.platform}",
            fontSize = 12.sp,
            color = NovaTextSecondary
          )

          Spacer(modifier = Modifier.height(3.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            // Signal Bars Indicator
            SignalStrengthBars(signalDbm = device.signalDbm)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "${device.signalDbm} dBm • ~${device.distanceMeters}m",
              fontSize = 10.sp,
              color = NovaTextMuted
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "• ${device.protocol}",
              fontSize = 10.sp,
              color = NovaPrimary,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }

      Spacer(modifier = Modifier.width(10.dp))

      Button(
        onClick = onClick,
        shape = RoundedCornerShape(100.dp),
        colors = ButtonDefaults.buttonColors(containerColor = NovaPrimary),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
        modifier = Modifier.testTag("send_to_scanned_device_${device.id}")
      ) {
        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(13.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Send", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
      }
    }
  }
}

@Composable
private fun SignalStrengthBars(signalDbm: Int) {
  val level = when {
    signalDbm >= -50 -> 4
    signalDbm >= -65 -> 3
    signalDbm >= -78 -> 2
    else -> 1
  }

  Row(
    verticalAlignment = Alignment.Bottom,
    horizontalArrangement = Arrangement.spacedBy(1.5.dp)
  ) {
    for (i in 1..4) {
      val barHeight = (4 + i * 2.5).dp
      val isActive = i <= level
      Box(
        modifier = Modifier
          .width(2.5.dp)
          .height(barHeight)
          .clip(RoundedCornerShape(1.dp))
          .background(if (isActive) NovaSuccess else Color(0xFFCBD5E1))
      )
    }
  }
}

@Composable
private fun TroubleshootItem(
  number: String,
  title: String,
  desc: String
) {
  Row(verticalAlignment = Alignment.Top) {
    Box(
      modifier = Modifier
        .size(18.dp)
        .clip(CircleShape)
        .background(NovaPrimary),
      contentAlignment = Alignment.Center
    ) {
      Text(number, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
    Spacer(modifier = Modifier.width(10.dp))
    Column {
      Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NovaTextPrimary)
      Text(desc, fontSize = 11.sp, color = NovaTextSecondary)
    }
  }
}

@Composable
private fun ManualConnectDialog(
  onDismiss: () -> Unit,
  onConnect: (String, String) -> Unit
) {
  var ipAddress by remember { mutableStateOf("192.168.1.105") }
  var pinCode by remember { mutableStateOf("7492") }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("manual_connect_dialog"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      border = BorderStroke(1.dp, NovaBorderSubtle)
    ) {
      Column(modifier = Modifier.padding(22.dp)) {
        Text(
          text = "Direct IP / PIN Pairing",
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = NovaTextPrimary
        )
        Text(
          text = "Connect directly across subnets or when broadcast discovery is blocked by your router.",
          fontSize = 12.sp,
          color = NovaTextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
          value = ipAddress,
          onValueChange = { ipAddress = it },
          label = { Text("Peer Device IP or Hostname") },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = pinCode,
          onValueChange = { pinCode = it },
          label = { Text("4-Digit Security PIN") },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(100.dp),
            border = BorderStroke(1.dp, NovaBorderSubtle)
          ) {
            Text("Cancel", fontSize = 13.sp, color = NovaTextSecondary)
          }

          Button(
            onClick = { onConnect(ipAddress, pinCode) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(100.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NovaPrimary)
          ) {
            Text("Connect", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
          }
        }
      }
    }
  }
}
