package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

/**
 * Manages the persistent background notification for fast access to the
 * file picker and QR code scanner even when NOVA-SEND is minimized or in the background.
 */
object QuickTransferNotificationManager {

  const val CHANNEL_ID = "novasend_quick_transfer_channel"
  const val NOTIFICATION_ID = 4802
  const val PREF_PERSISTENT_NOTIFICATION = "pref_persistent_quick_notification"

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val name = "Quick Transfer & File Picker"
      val descriptionText = "Persistent background access to send files and scan devices"
      val importance = NotificationManager.IMPORTANCE_LOW
      val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
        description = descriptionText
        setShowBadge(false)
        enableVibration(false)
      }
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(channel)
    }
  }

  fun showPersistentNotification(context: Context) {
    createNotificationChannel(context)

    // Main tap opens the Send / File Picker screen
    val mainIntent = Intent(context, MainActivity::class.java).apply {
      action = "com.example.ACTION_QUICK_SEND"
      flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val mainPendingIntent = PendingIntent.getActivity(
      context,
      101,
      mainIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Action 1: Quick Send Files (File Picker)
    val sendIntent = Intent(context, MainActivity::class.java).apply {
      action = "com.example.ACTION_QUICK_SEND"
      flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val sendPendingIntent = PendingIntent.getActivity(
      context,
      102,
      sendIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Action 2: Scan QR Code
    val qrIntent = Intent(context, MainActivity::class.java).apply {
      action = "com.example.ACTION_SCAN_QR"
      flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val qrPendingIntent = PendingIntent.getActivity(
      context,
      103,
      qrIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_shortcut_send)
      .setContentTitle("NOVA-SEND • Quick Transfer Ready")
      .setContentText("Tap to browse files or scan QR to connect nearby devices")
      .setContentIntent(mainPendingIntent)
      .setOngoing(true)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .setCategory(NotificationCompat.CATEGORY_SERVICE)
      .addAction(
        R.drawable.ic_shortcut_send,
        "Send Files",
        sendPendingIntent
      )
      .addAction(
        R.drawable.ic_shortcut_qr,
        "Scan QR",
        qrPendingIntent
      )

    try {
      val notificationManager = NotificationManagerCompat.from(context)
      notificationManager.notify(NOTIFICATION_ID, builder.build())
      context.getSharedPreferences("novasend_prefs", Context.MODE_PRIVATE)
        .edit()
        .putBoolean(PREF_PERSISTENT_NOTIFICATION, true)
        .apply()
    } catch (e: SecurityException) {
      // Missing POST_NOTIFICATIONS on Android 13+
    }
  }

  fun cancelPersistentNotification(context: Context) {
    val notificationManager = NotificationManagerCompat.from(context)
    notificationManager.cancel(NOTIFICATION_ID)
    context.getSharedPreferences("novasend_prefs", Context.MODE_PRIVATE)
      .edit()
      .putBoolean(PREF_PERSISTENT_NOTIFICATION, false)
      .apply()
  }

  fun isPersistentNotificationEnabled(context: Context): Boolean {
    return context.getSharedPreferences("novasend_prefs", Context.MODE_PRIVATE)
      .getBoolean(PREF_PERSISTENT_NOTIFICATION, true) // default true for convenience
  }
}
