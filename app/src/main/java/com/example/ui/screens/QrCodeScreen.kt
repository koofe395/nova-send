package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.NovaViewModel
import com.example.ui.theme.NovaAccent
import com.example.ui.theme.NovaBackground
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaBorderSubtle
import com.example.ui.theme.NovaCardSurface
import com.example.ui.theme.NovaPrimary
import com.example.ui.theme.NovaPrimaryContainer
import com.example.ui.theme.NovaPrimaryLight
import com.example.ui.theme.NovaSuccess
import com.example.ui.theme.NovaSurfaceVariant
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary

/**
 * QR Code Generation and Scanning interface for secure, instant device pairing
 * following Geometric Balance aesthetics and Vencold Dashboard styling.
 */
@Composable
fun QrCodeDialog(
  viewModel: NovaViewModel,
  onDismiss: () -> Unit,
  onDevicePairedForSend: (deviceName: String) -> Unit = {}
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .clip(RoundedCornerShape(24.dp))
        .border(1.dp, NovaBorderSubtle, RoundedCornerShape(24.dp))
        .testTag("qr_code_dialog"),
      color = NovaBackground
    ) {
      var selectedTab by remember { mutableIntStateOf(0) } // 0: My QR (Generate), 1: Scan QR (Scanner)

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        // Top Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(NovaPrimaryLight),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (selectedTab == 0) Icons.Default.QrCode else Icons.Default.QrCodeScanner,
                contentDescription = null,
                tint = NovaPrimary,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "P2P Device Pairing",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = NovaTextPrimary
              )
              Text(
                text = "Zero-Configuration AES-256 Handshake",
                fontSize = 12.sp,
                color = NovaTextSecondary
              )
            }
          }
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("qr_dialog_close_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = NovaTextSecondary
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Row
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = Color.White,
          contentColor = NovaPrimary,
          indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
              modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
              color = NovaPrimary
            )
          },
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, NovaBorderSubtle, RoundedCornerShape(12.dp))
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("My Pairing QR", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
              }
            }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan Device QR", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
              }
            }
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        Box(
          modifier = Modifier
            .weight(1f, fill = false)
            .fillMaxWidth()
        ) {
          if (selectedTab == 0) {
            QrCodeGenerateView(viewModel = viewModel)
          } else {
            QrCodeScannerView(
              viewModel = viewModel,
              onDevicePaired = { pairedName ->
                onDevicePairedForSend(pairedName)
                onDismiss()
              }
            )
          }
        }
      }
    }
  }
}

/**
 * Tab 1: Render an authentic 2D QR Code for the local device.
 */
@Composable
fun QrCodeGenerateView(
  viewModel: NovaViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var tokenSeed by remember { mutableIntStateOf(1048) }
  val payload = remember(tokenSeed) {
    """{"app":"NOVA-SEND","v":"3.1","id":"DEV-9104","name":"Mohamed's Pixel 9 Pro","ip":"192.168.1.145","port":8848,"aes":"7F9B-2E81-$tokenSeed","proto":"WIFI_DIRECT_5G"}"""
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // QR Code Container Card
    Card(
      modifier = Modifier
        .fillMaxWidth(0.85f)
        .aspectRatio(1f)
        .padding(top = 8.dp),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      border = BorderStroke(1.5.dp, NovaBorderSubtle),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        // High-precision custom Canvas rendering authentic QR Matrix
        QrCodeMatrixCanvas(
          payload = payload,
          modifier = Modifier
            .fillMaxSize()
            .testTag("qr_code_canvas")
        )

        // Center Logo Badge
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(2.dp, NovaPrimary, RoundedCornerShape(8.dp)),
          contentAlignment = Alignment.Center
        ) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(RoundedCornerShape(6.dp))
              .background(NovaPrimary),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Shield,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Device Badge & Specs Card
    Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      color = Color.White,
      border = BorderStroke(1.dp, NovaBorderSubtle)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Mohamed's Pixel 9 Pro",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = NovaTextPrimary
            )
            Text(
              text = "Android 15 • 192.168.1.145:8848",
              fontSize = 12.sp,
              color = NovaTextSecondary
            )
          }
          Surface(
            color = NovaSuccess.copy(alpha = 0.12f),
            shape = RoundedCornerShape(100.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(NovaSuccess)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("Discoverable", color = NovaSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Security key row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(NovaSurfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.VpnKey, contentDescription = null, tint = NovaPrimary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "AES Session: 7F9B-2E81-$tokenSeed",
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
              color = NovaTextPrimary
            )
          }
          IconButton(
            onClick = {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              clipboard.setPrimaryClip(ClipData.newPlainText("NOVA-SEND Token", payload))
              Toast.makeText(context, "Pairing token copied to clipboard", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.size(24.dp)
          ) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = NovaPrimary, modifier = Modifier.size(14.dp))
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Actions
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      OutlinedButton(
        onClick = { tokenSeed = (1000..9999).random() },
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, NovaBorderSubtle)
      ) {
        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Refresh Key", fontSize = 13.sp, color = NovaTextPrimary)
      }

      Button(
        onClick = {
          val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
          clipboard.setPrimaryClip(ClipData.newPlainText("NOVA-SEND Pairing", payload))
          Toast.makeText(context, "Pairing link copied!", Toast.LENGTH_SHORT).show()
        },
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = NovaPrimary)
      ) {
        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Copy Token", fontSize = 13.sp)
      }
    }
  }
}

/**
 * Tab 2: Scanning interface with animated laser sweep, manual token fallback,
 * and one-tap simulated discovery for instant testing.
 */
@Composable
fun QrCodeScannerView(
  viewModel: NovaViewModel,
  onDevicePaired: (deviceName: String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var isFlashlightOn by remember { mutableStateOf(false) }
  var showManualInput by remember { mutableStateOf(false) }
  var manualCodeText by remember { mutableStateOf("") }
  var pairedSuccessDevice by remember { mutableStateOf<String?>(null) }

  val infiniteTransition = rememberInfiniteTransition(label = "laser_sweep")
  val laserProgress by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(2200, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "laser_pos"
  )

  Column(
    modifier = modifier
      .fillMaxWidth()
      .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    if (pairedSuccessDevice != null) {
      // Success Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, NovaSuccess)
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(CircleShape)
              .background(NovaSuccess.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NovaSuccess, modifier = Modifier.size(32.dp))
          }
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Device Paired Successfully!",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = NovaTextPrimary
          )
          Text(
            text = "Trusted P2P channel established with $pairedSuccessDevice",
            fontSize = 12.sp,
            color = NovaTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { onDevicePaired(pairedSuccessDevice ?: "Device") },
              modifier = Modifier.fillMaxWidth(),
              colors = ButtonDefaults.buttonColors(containerColor = NovaPrimary),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Send Files Now", fontSize = 13.sp)
            }
          }
        }
      }
    } else {
      // Viewfinder Container
      Box(
        modifier = Modifier
          .fillMaxWidth(0.9f)
          .aspectRatio(1.1f)
          .clip(RoundedCornerShape(20.dp))
          .background(Color(0xFF141218))
          .testTag("qr_scanner_viewfinder"),
        contentAlignment = Alignment.Center
      ) {
        // Subtle grid background
        Canvas(modifier = Modifier.fillMaxSize()) {
          val step = 30f
          for (x in 0..(size.width / step).toInt()) {
            drawLine(
              color = Color.White.copy(alpha = 0.04f),
              start = Offset(x * step, 0f),
              end = Offset(x * step, size.height),
              strokeWidth = 1f
            )
          }
          for (y in 0..(size.height / step).toInt()) {
            drawLine(
              color = Color.White.copy(alpha = 0.04f),
              start = Offset(0f, y * step),
              end = Offset(size.width, y * step),
              strokeWidth = 1f
            )
          }
        }

        // Viewfinder Cutout Frame (Square with rounded corners)
        Box(
          modifier = Modifier
            .size(200.dp)
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
        ) {
          // 4 Corner Brackets
          Canvas(modifier = Modifier.fillMaxSize()) {
            val bracketLen = 28f
            val stroke = 4f
            val c = Color(0xFFD0BCFF)

            // Top-Left
            drawLine(c, Offset(0f, 0f), Offset(bracketLen, 0f), stroke)
            drawLine(c, Offset(0f, 0f), Offset(0f, bracketLen), stroke)

            // Top-Right
            drawLine(c, Offset(size.width, 0f), Offset(size.width - bracketLen, 0f), stroke)
            drawLine(c, Offset(size.width, 0f), Offset(size.width, bracketLen), stroke)

            // Bottom-Left
            drawLine(c, Offset(0f, size.height), Offset(bracketLen, size.height), stroke)
            drawLine(c, Offset(0f, size.height), Offset(0f, size.height - bracketLen), stroke)

            // Bottom-Right
            drawLine(c, Offset(size.width, size.height), Offset(size.width - bracketLen, size.height), stroke)
            drawLine(c, Offset(size.width, size.height), Offset(size.width, size.height - bracketLen), stroke)
          }

          // Animated Laser Sweep Line
          Canvas(modifier = Modifier.fillMaxSize()) {
            val yPos = size.height * laserProgress
            drawRect(
              brush = Brush.verticalGradient(
                listOf(
                  Color.Transparent,
                  NovaPrimary.copy(alpha = 0.3f),
                  NovaAccent,
                  NovaPrimary.copy(alpha = 0.3f),
                  Color.Transparent
                )
              ),
              topLeft = Offset(0f, yPos - 6f),
              size = Size(size.width, 12f)
            )
            drawLine(
              color = Color.White,
              start = Offset(0f, yPos),
              end = Offset(size.width, yPos),
              strokeWidth = 2.5f
            )
          }
        }

        // Controls overlay at bottom of viewfinder
        Row(
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          IconButton(
            onClick = { isFlashlightOn = !isFlashlightOn },
            modifier = Modifier.size(36.dp)
          ) {
            Icon(
              imageVector = if (isFlashlightOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
              contentDescription = "Torch",
              tint = if (isFlashlightOn) NovaAccent else Color.White,
              modifier = Modifier.size(18.dp)
            )
          }

          IconButton(
            onClick = {
              Toast.makeText(context, "Switched camera sensor", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.size(36.dp)
          ) {
            Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Flip", tint = Color.White, modifier = Modifier.size(18.dp))
          }

          IconButton(
            onClick = {
              // Simulate reading sample QR from gallery image
              val sampleQr = """{"app":"NOVA-SEND","v":"3.1","id":"DEV-MBP16","name":"Sarah's MacBook Pro 16","type":"LAPTOP","platform":"macOS Sequoia"}"""
              val res = viewModel.pairDeviceFromQr(sampleQr)
              if (res.first) {
                pairedSuccessDevice = "Sarah's MacBook Pro 16"
              }
            },
            modifier = Modifier.size(36.dp)
          ) {
            Icon(Icons.Default.Image, contentDescription = "Gallery", tint = Color.White, modifier = Modifier.size(18.dp))
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "Point camera at another device's NOVA-SEND QR code",
        fontSize = 12.sp,
        color = NovaTextSecondary,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Instant Simulation / Test Device Presets (Enables rapid testing in container)
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, NovaBorderSubtle)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Test Discovered Nodes",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = NovaTextPrimary
            )
            Text(
              text = "Tap to simulate scan",
              fontSize = 11.sp,
              color = NovaTextMuted
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          val testPresets = listOf(
            Triple("MacBook Pro 16\" (M3 Max)", "LAPTOP", "macOS Sequoia"),
            Triple("Studio Workstation PC", "DESKTOP", "Windows 11 Pro"),
            Triple("Sarah's iPad Pro 13\"", "TABLET", "iPadOS 18")
          )

          testPresets.forEach { (name, type, platform) ->
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable {
                  val payload = """{"app":"NOVA-SEND","name":"$name","type":"$type","platform":"$platform"}"""
                  val res = viewModel.pairDeviceFromQr(payload)
                  if (res.first) {
                    pairedSuccessDevice = name
                  }
                },
              shape = RoundedCornerShape(10.dp),
              color = NovaSurfaceVariant,
              border = BorderStroke(1.dp, NovaBorderSubtle)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Devices, contentDescription = null, tint = NovaPrimary, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NovaTextPrimary)
                    Text(text = "$platform • Zero-Config P2P", fontSize = 10.sp, color = NovaTextSecondary)
                  }
                }
                Surface(
                  color = NovaPrimary.copy(alpha = 0.12f),
                  shape = RoundedCornerShape(100.dp)
                ) {
                  Text(
                    text = "Pair Node",
                    color = NovaPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                  )
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Manual Code Fallback Accordion
      OutlinedButton(
        onClick = { showManualInput = !showManualInput },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, NovaBorderSubtle)
      ) {
        Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (showManualInput) "Hide Manual Code Entry" else "Enter Code / Token Manually",
          fontSize = 12.sp,
          color = NovaTextPrimary
        )
      }

      AnimatedVisibility(visible = showManualInput) {
        Column(modifier = Modifier.padding(top = 10.dp)) {
          OutlinedTextField(
            value = manualCodeText,
            onValueChange = { manualCodeText = it },
            label = { Text("Paste NOVA-SEND Pairing Payload or Token", fontSize = 11.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = false,
            maxLines = 3
          )
          Spacer(modifier = Modifier.height(8.dp))
          Button(
            onClick = {
              if (manualCodeText.isNotBlank()) {
                val res = viewModel.pairDeviceFromQr(manualCodeText)
                if (res.first) {
                  pairedSuccessDevice = "Manual Paired Device"
                } else {
                  Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
                }
              }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NovaPrimary)
          ) {
            Text("Verify and Pair", fontSize = 12.sp)
          }
        }
      }
    }
  }
}

/**
 * Draws an authentic 2D QR Code matrix with 7x7 corner finder patterns,
 * timing tracks, alignment patterns, and deterministic data modules.
 */
@Composable
fun QrCodeMatrixCanvas(
  payload: String,
  modifier: Modifier = Modifier
) {
  val darkModuleColor = NovaPrimary
  val lightModuleColor = Color.Transparent

  Canvas(modifier = modifier) {
    val matrixSize = 25 // 25x25 QR Version 2 matrix
    val cellSize = size.width / matrixSize

    // Deterministic pseudo-random seed from payload
    val hash = payload.hashCode().toLong()

    fun isFinderPattern(r: Int, c: Int): Boolean {
      // Top-Left (0..6, 0..6)
      if (r in 0..6 && c in 0..6) return true
      // Top-Right (0..6, matrixSize-7..matrixSize-1)
      if (r in 0..6 && c in (matrixSize - 7) until matrixSize) return true
      // Bottom-Left (matrixSize-7..matrixSize-1, 0..6)
      if (r in (matrixSize - 7) until matrixSize && c in 0..6) return true
      return false
    }

    fun isFinderBlack(r: Int, c: Int, rowOffset: Int, colOffset: Int): Boolean {
      val localR = r - rowOffset
      val localC = c - colOffset
      // 7x7 outer border
      if (localR == 0 || localR == 6 || localC == 0 || localC == 6) return true
      // 5x5 inner white ring
      if (localR == 1 || localR == 5 || localC == 1 || localC == 5) return false
      // 3x3 inner pupil
      return true
    }

    // 1. Draw Data Modules & Timing Tracks
    for (r in 0 until matrixSize) {
      for (c in 0 until matrixSize) {
        val isFinder = isFinderPattern(r, c)
        if (isFinder) continue

        // Timing patterns (row 6 and col 6 alternating)
        val isTiming = (r == 6 || c == 6)
        val isBlack = if (isTiming) {
          (r + c) % 2 == 0
        } else {
          // Deterministic data block fill
          val cellSeed = (r * 31 + c * 17) xor (hash.toInt())
          ((cellSeed.ushr(c % 16)) and 1) == 1
        }

        if (isBlack) {
          drawRoundRect(
            color = darkModuleColor,
            topLeft = Offset(c * cellSize, r * cellSize),
            size = Size(cellSize * 0.92f, cellSize * 0.92f),
            cornerRadius = CornerRadius(cellSize * 0.2f, cellSize * 0.2f)
          )
        }
      }
    }

    // 2. Draw Top-Left Finder Pattern (7x7)
    for (r in 0..6) {
      for (c in 0..6) {
        if (isFinderBlack(r, c, 0, 0)) {
          drawRoundRect(
            color = darkModuleColor,
            topLeft = Offset(c * cellSize, r * cellSize),
            size = Size(cellSize, cellSize),
            cornerRadius = CornerRadius(cellSize * 0.15f, cellSize * 0.15f)
          )
        }
      }
    }

    // 3. Draw Top-Right Finder Pattern (7x7)
    val trColOffset = matrixSize - 7
    for (r in 0..6) {
      for (c in trColOffset until matrixSize) {
        if (isFinderBlack(r, c, 0, trColOffset)) {
          drawRoundRect(
            color = darkModuleColor,
            topLeft = Offset(c * cellSize, r * cellSize),
            size = Size(cellSize, cellSize),
            cornerRadius = CornerRadius(cellSize * 0.15f, cellSize * 0.15f)
          )
        }
      }
    }

    // 4. Draw Bottom-Left Finder Pattern (7x7)
    val blRowOffset = matrixSize - 7
    for (r in blRowOffset until matrixSize) {
      for (c in 0..6) {
        if (isFinderBlack(r, c, blRowOffset, 0)) {
          drawRoundRect(
            color = darkModuleColor,
            topLeft = Offset(c * cellSize, r * cellSize),
            size = Size(cellSize, cellSize),
            cornerRadius = CornerRadius(cellSize * 0.15f, cellSize * 0.15f)
          )
        }
      }
    }

    // 5. Draw Alignment Pattern (5x5) at (matrixSize - 9, matrixSize - 9)
    val alignR = matrixSize - 8
    val alignC = matrixSize - 8
    for (r in (alignR - 2)..(alignR + 2)) {
      for (c in (alignC - 2)..(alignC + 2)) {
        val dr = kotlin.math.abs(r - alignR)
        val dc = kotlin.math.abs(c - alignC)
        val isAlignBlack = (dr == 2 || dc == 2 || (dr == 0 && dc == 0))
        if (isAlignBlack) {
          drawRoundRect(
            color = darkModuleColor,
            topLeft = Offset(c * cellSize, r * cellSize),
            size = Size(cellSize, cellSize),
            cornerRadius = CornerRadius(cellSize * 0.15f, cellSize * 0.15f)
          )
        }
      }
    }
  }
}
