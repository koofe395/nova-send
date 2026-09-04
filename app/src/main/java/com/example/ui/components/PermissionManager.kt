package com.example.ui.components

import android.app.Activity
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.ui.theme.NovaBackground
import com.example.ui.theme.NovaBorderSubtle
import com.example.ui.theme.NovaPrimary
import com.example.ui.theme.NovaPrimaryLight
import com.example.ui.theme.NovaSuccess
import com.example.ui.theme.NovaSurfaceVariant
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.util.PermissionHelper

/**
 * State holder for near-device and storage permissions.
 */
class NearDevicePermissionState(
  val hasPermissions: Boolean,
  val isDenied: Boolean,
  val isPermanentlyDenied: Boolean,
  val showRationaleDialog: Boolean,
  val showDeniedDialog: Boolean,
  val onLaunchPermissionRequest: () -> Unit,
  val onOpenSettings: () -> Unit,
  val onDismissRationale: () -> Unit,
  val onDismissDeniedDialog: () -> Unit,
  val onConfirmRationaleAndRequest: () -> Unit
)

@Composable
fun rememberNearDevicePermissionState(): NearDevicePermissionState {
  val context = LocalContext.current
  val activity = context as? Activity
  val lifecycleOwner = LocalLifecycleOwner.current

  var hasPermissions by remember {
    mutableStateOf(PermissionHelper.hasNearDevicePermissions(context))
  }
  var isDenied by remember { mutableStateOf(false) }
  var isPermanentlyDenied by remember { mutableStateOf(false) }
  var showRationaleDialog by remember { mutableStateOf(false) }
  var showDeniedDialog by remember { mutableStateOf(false) }
  var requestAttemptCount by remember { mutableStateOf(0) }

  // Re-check permissions whenever app returns to foreground (e.g. from System Settings)
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        val current = PermissionHelper.hasNearDevicePermissions(context)
        hasPermissions = current
        if (current) {
          isDenied = false
          isPermanentlyDenied = false
          showRationaleDialog = false
          showDeniedDialog = false
        }
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { results ->
    requestAttemptCount++
    val allGranted = results.values.all { it }
    hasPermissions = allGranted

    if (!allGranted) {
      isDenied = true
      // If rationale is false but we were denied after attempting, user likely selected "Don't ask again"
      val shouldShowRationale = activity?.let { PermissionHelper.shouldShowNearDeviceRationale(it) } ?: false
      if (!shouldShowRationale && requestAttemptCount >= 1) {
        isPermanentlyDenied = true
      }
      showDeniedDialog = true
    } else {
      isDenied = false
      isPermanentlyDenied = false
      showDeniedDialog = false
    }
  }

  val requestPermissionsAction: () -> Unit = {
    if (PermissionHelper.hasNearDevicePermissions(context)) {
      hasPermissions = true
    } else {
      val shouldShow = activity?.let { PermissionHelper.shouldShowNearDeviceRationale(it) } ?: false
      if (shouldShow || requestAttemptCount == 0) {
        // Show informative rationale before triggering system prompt
        showRationaleDialog = true
      } else if (isPermanentlyDenied) {
        showDeniedDialog = true
      } else {
        permissionLauncher.launch(PermissionHelper.getNearDevicePermissions().toTypedArray())
      }
    }
  }

  val confirmRationaleAction: () -> Unit = {
    showRationaleDialog = false
    permissionLauncher.launch(PermissionHelper.getNearDevicePermissions().toTypedArray())
  }

  return remember(
    hasPermissions,
    isDenied,
    isPermanentlyDenied,
    showRationaleDialog,
    showDeniedDialog
  ) {
    NearDevicePermissionState(
      hasPermissions = hasPermissions,
      isDenied = isDenied,
      isPermanentlyDenied = isPermanentlyDenied,
      showRationaleDialog = showRationaleDialog,
      showDeniedDialog = showDeniedDialog,
      onLaunchPermissionRequest = requestPermissionsAction,
      onOpenSettings = {
        PermissionHelper.openAppSettings(context)
      },
      onDismissRationale = { showRationaleDialog = false },
      onDismissDeniedDialog = { showDeniedDialog = false },
      onConfirmRationaleAndRequest = confirmRationaleAction
    )
  }
}

/**
 * Pre-permission Rationale Dialog explaining why Near-Device & Wi-Fi permissions are needed.
 */
@Composable
fun NearDeviceRationaleDialog(
  onDismiss: () -> Unit,
  onGrantClick: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("permission_rationale_dialog"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      border = BorderStroke(1.dp, NovaBorderSubtle)
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(NovaPrimaryLight),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.NearMe,
            contentDescription = null,
            tint = NovaPrimary,
            modifier = Modifier.size(28.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Near-Device Discovery",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = NovaTextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "To find nearby computers, phones, and tablets for high-speed file transfers without internet, NOVA-SEND requires local radio permissions.",
          fontSize = 13.sp,
          color = NovaTextSecondary,
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Feature list
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NovaSurfaceVariant)
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Wifi, contentDescription = null, tint = NovaPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("Nearby Wi-Fi Devices", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NovaTextPrimary)
              Text("Discovers peer devices on Wi-Fi Direct & LAN", fontSize = 11.sp, color = NovaTextSecondary)
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Bluetooth, contentDescription = null, tint = NovaPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("Bluetooth Low Energy", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NovaTextPrimary)
              Text("Zero-touch beacon discovery and pairing", fontSize = 11.sp, color = NovaTextSecondary)
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.FolderShared, contentDescription = null, tint = NovaPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("Secure File Access", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NovaTextPrimary)
              Text("Scoped access only to files you choose to send", fontSize = 11.sp, color = NovaTextSecondary)
            }
          }
        }

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
            Text("Not Now", fontSize = 13.sp, color = NovaTextSecondary)
          }

          Button(
            onClick = onGrantClick,
            modifier = Modifier
              .weight(1f)
              .testTag("confirm_grant_permission_btn"),
            shape = RoundedCornerShape(100.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NovaPrimary)
          ) {
            Text("Continue", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
          }
        }
      }
    }
  }
}

/**
 * Dialog shown when near-device permission was denied or permanently denied.
 * Provides explicit guidance and seamless redirect to System Settings.
 */
@Composable
fun NearDeviceDeniedDialog(
  isPermanentlyDenied: Boolean,
  onDismiss: () -> Unit,
  onRetry: () -> Unit,
  onOpenSettings: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("permission_denied_dialog"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      border = BorderStroke(1.dp, NovaBorderSubtle)
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(Color(0xFFFEF2F2)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = Color(0xFFDC2626),
            modifier = Modifier.size(28.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = if (isPermanentlyDenied) "Permission Required in Settings" else "Permission Needed",
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = NovaTextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = if (isPermanentlyDenied) {
            "Nearby device discovery was disabled. Because Android requires manual authorization for local radio access, please enable 'Nearby devices' in app settings."
          } else {
            "Nearby device discovery was declined. Without this permission, NOVA-SEND cannot detect other phones or computers nearby."
          },
          fontSize = 13.sp,
          color = NovaTextSecondary,
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        if (isPermanentlyDenied) {
          Button(
            onClick = onOpenSettings,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("open_settings_redirect_btn"),
            shape = RoundedCornerShape(100.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NovaPrimary)
          ) {
            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open App Settings", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
          }
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(100.dp),
            border = BorderStroke(1.dp, NovaBorderSubtle)
          ) {
            Text("Cancel", fontSize = 13.sp, color = NovaTextSecondary)
          }
        } else {
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
              Text("Dismiss", fontSize = 13.sp, color = NovaTextSecondary)
            }

            Button(
              onClick = onRetry,
              modifier = Modifier
                .weight(1f)
                .testTag("retry_permission_btn"),
              shape = RoundedCornerShape(100.dp),
              colors = ButtonDefaults.buttonColors(containerColor = NovaPrimary)
            ) {
              Text("Try Again", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
          }
        }
      }
    }
  }
}

/**
 * Status Banner / Card displaying live connectivity & permission status.
 */
@Composable
fun NearDevicePermissionBanner(
  permissionState: NearDevicePermissionState,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (permissionState.hasPermissions) {
        Color(0xFFF0FDF4)
      } else {
        Color(0xFFFFFBEB)
      }
    ),
    border = BorderStroke(
      1.dp,
      if (permissionState.hasPermissions) Color(0xFFBBF7D0) else Color(0xFFFDE68A)
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
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
              if (permissionState.hasPermissions) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (permissionState.hasPermissions) Icons.Default.CheckCircle else Icons.Default.NearMe,
            contentDescription = null,
            tint = if (permissionState.hasPermissions) NovaSuccess else Color(0xFFD97706),
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (permissionState.hasPermissions) {
              "Near-Device Radio Active"
            } else {
              "Device Discovery Permissions"
            },
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = NovaTextPrimary
          )
          Text(
            text = if (permissionState.hasPermissions) {
              "Wi-Fi Direct & Bluetooth scanning enabled"
            } else if (permissionState.isPermanentlyDenied) {
              "Access blocked in settings — tap to resolve"
            } else {
              "Required for detecting nearby sharing peers"
            },
            fontSize = 11.sp,
            color = NovaTextSecondary
          )
        }
      }

      if (!permissionState.hasPermissions) {
        Spacer(modifier = Modifier.width(8.dp))
        Button(
          onClick = {
            if (permissionState.isPermanentlyDenied) {
              permissionState.onOpenSettings()
            } else {
              permissionState.onLaunchPermissionRequest()
            }
          },
          shape = RoundedCornerShape(100.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (permissionState.isPermanentlyDenied) Color(0xFFD97706) else NovaPrimary
          ),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          modifier = Modifier.testTag("permission_banner_action_btn")
        ) {
          Text(
            text = if (permissionState.isPermanentlyDenied) "Settings" else "Grant",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }

  // Render Dialogs when triggered
  if (permissionState.showRationaleDialog) {
    NearDeviceRationaleDialog(
      onDismiss = permissionState.onDismissRationale,
      onGrantClick = permissionState.onConfirmRationaleAndRequest
    )
  }

  if (permissionState.showDeniedDialog) {
    NearDeviceDeniedDialog(
      isPermanentlyDenied = permissionState.isPermanentlyDenied,
      onDismiss = permissionState.onDismissDeniedDialog,
      onRetry = permissionState.onLaunchPermissionRequest,
      onOpenSettings = permissionState.onOpenSettings
    )
  }
}
