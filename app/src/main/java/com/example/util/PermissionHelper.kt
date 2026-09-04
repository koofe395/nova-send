package com.example.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionHelper {

  /**
   * Returns the list of runtime permissions required for discovering and
   * connecting to nearby devices via Wi-Fi Direct and Bluetooth.
   */
  fun getNearDevicePermissions(): List<String> {
    val list = mutableListOf<String>()
    when {
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
        // Android 13+ (API 33+)
        list.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        list.add(Manifest.permission.BLUETOOTH_SCAN)
        list.add(Manifest.permission.BLUETOOTH_CONNECT)
      }
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        // Android 12 & 12L (API 31-32)
        list.add(Manifest.permission.BLUETOOTH_SCAN)
        list.add(Manifest.permission.BLUETOOTH_CONNECT)
        list.add(Manifest.permission.ACCESS_FINE_LOCATION)
      }
      else -> {
        // Android 11 and earlier
        list.add(Manifest.permission.ACCESS_FINE_LOCATION)
        list.add(Manifest.permission.ACCESS_COARSE_LOCATION)
      }
    }
    return list
  }

  /**
   * Returns the storage permissions required for legacy devices.
   * On Android 13+, scoped storage and Android Document Provider (SAF) / Photo Picker
   * are zero-permission.
   */
  fun getStoragePermissions(): List<String> {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
      listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    } else {
      emptyList()
    }
  }

  /**
   * Checks whether all near-device connectivity permissions are currently granted.
   */
  fun hasNearDevicePermissions(context: Context): Boolean {
    val required = getNearDevicePermissions()
    return required.all { perm ->
      ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
    }
  }

  /**
   * Checks whether storage permission is granted (or not required on modern Android).
   */
  fun hasStoragePermission(context: Context): Boolean {
    val required = getStoragePermissions()
    if (required.isEmpty()) return true
    return required.all { perm ->
      ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
    }
  }

  /**
   * Checks if any required permission should show a rationale.
   */
  fun shouldShowNearDeviceRationale(activity: Activity): Boolean {
    val required = getNearDevicePermissions()
    return required.any { perm ->
      ActivityCompat.shouldShowRequestPermissionRationale(activity, perm)
    }
  }

  /**
   * Opens the system App Details settings page so the user can manually enable permissions.
   */
  fun openAppSettings(context: Context) {
    try {
      val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    } catch (e: Exception) {
      val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(fallbackIntent)
    }
  }
}
