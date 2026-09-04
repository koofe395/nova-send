package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transfers")
data class TransferEntity(
  @PrimaryKey val id: String,
  val direction: String, // "SEND" or "RECEIVE"
  val senderName: String,
  val senderUsername: String,
  val senderDevice: String,
  val receiverName: String,
  val receiverUsername: String,
  val receiverDevice: String,
  val totalFiles: Int,
  val totalBytes: Long,
  val transferredBytes: Long,
  val speed: String,
  val status: String, // PENDING, WAITING_ACCEPTANCE, ACCEPTED, CONNECTING, TRANSFERRING, VERIFYING, COMPLETED, PAUSED, CANCELLED, FAILED
  val checksum: String,
  val securityStatus: String,
  val createdAt: Long,
  val completedAt: Long?,
  val fileNamesSummary: String,
  val primaryFileType: String, // "DOCUMENT", "IMAGE", "VIDEO", "AUDIO", "ARCHIVE", "TEXT", "MIXED"
  val failureReason: String? = null
)

@Entity(tableName = "transfer_files")
data class TransferFileEntity(
  @PrimaryKey val id: String,
  val transferId: String,
  val fileName: String,
  val fileSize: Long,
  val fileType: String,
  val progress: Float, // 0.0 to 1.0
  val status: String, // QUEUED, TRANSFERRING, VERIFYING, COMPLETED, FAILED
  val checksum: String,
)

@Entity(tableName = "devices")
data class DeviceEntity(
  @PrimaryKey val id: String,
  val name: String,
  val type: String, // "LAPTOP", "DESKTOP", "PHONE", "TABLET", "SERVER"
  val platform: String, // "macOS", "Windows", "Android", "iOS", "Linux"
  val isOnline: Boolean,
  val isTrusted: Boolean,
  val lastSeen: String,
  val connectionType: String,
)

@Entity(tableName = "contacts")
data class ContactEntity(
  @PrimaryKey val id: String,
  val name: String,
  val username: String,
  val avatarInitials: String,
  val deviceName: String,
  val isOnline: Boolean,
  val isVerified: Boolean,
  val isFavorite: Boolean,
  val avatarColorHex: Long,
)

@Entity(tableName = "messages")
data class MessageEntity(
  @PrimaryKey val id: String,
  val contactId: String,
  val isMe: Boolean,
  val senderName: String,
  val content: String,
  val isTransferCard: Boolean,
  val transferFileName: String? = null,
  val transferFileSize: String? = null,
  val timestamp: Long,
)

@Entity(tableName = "notifications")
data class NotificationEntity(
  @PrimaryKey val id: String,
  val title: String,
  val body: String,
  val type: String, // "TRANSFER_REQUEST", "TRANSFER_ACCEPTED", "TRANSFER_COMPLETED", "DEVICE_CONNECTED", "SECURITY"
  val timestamp: Long,
  val isRead: Boolean,
  val actionPayload: String? = null,
)

// UI Model for Content Selected in Send Workflow
data class SelectedFileItem(
  val id: String,
  val name: String,
  val sizeBytes: Long,
  val type: String, // IMAGE, VIDEO, DOCUMENT, AUDIO, ARCHIVE, CODE, TEXT
  val extension: String,
  val isCustom: Boolean = false,
)
