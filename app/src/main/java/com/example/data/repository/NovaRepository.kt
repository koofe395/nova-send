package com.example.data.repository

import com.example.data.local.ContactDao
import com.example.data.local.DeviceDao
import com.example.data.local.MessageDao
import com.example.data.local.NotificationDao
import com.example.data.local.TransferDao
import com.example.data.model.ContactEntity
import com.example.data.model.DeviceEntity
import com.example.data.model.MessageEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.TransferEntity
import com.example.data.model.TransferFileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NovaRepository(
  private val transferDao: TransferDao,
  private val deviceDao: DeviceDao,
  private val contactDao: ContactDao,
  private val messageDao: MessageDao,
  private val notificationDao: NotificationDao,
) {
  val allTransfers: Flow<List<TransferEntity>> = transferDao.getAllTransfers()
  val activeTransfers: Flow<List<TransferEntity>> = transferDao.getActiveTransfers()
  val successfulTransfers: Flow<List<TransferEntity>> = transferDao.getSuccessfulTransfers()
  val failedTransfers: Flow<List<TransferEntity>> = transferDao.getFailedTransfers()
  val recentActivity: Flow<List<TransferEntity>> = transferDao.getRecentActivity(50)
  val allDevices: Flow<List<DeviceEntity>> = deviceDao.getAllDevices()
  val allContacts: Flow<List<ContactEntity>> = contactDao.getAllContacts()
  val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()
  val unreadNotifications: Flow<Int> = notificationDao.getUnreadCount()

  init {
    CoroutineScope(Dispatchers.IO).launch {
      seedDefaultDataIfEmpty()
    }
  }

  private suspend fun seedDefaultDataIfEmpty() {
    val existingDevices = deviceDao.getAllDevices().first()
    if (existingDevices.isEmpty()) {
      deviceDao.insertDevices(
        listOf(
          DeviceEntity(
            id = "DEV-01",
            name = "MacBook Pro 16\" (M3 Max)",
            type = "LAPTOP",
            platform = "macOS Sequoia",
            isOnline = true,
            isTrusted = true,
            lastSeen = "Active now",
            connectionType = "Direct Wi-Fi 6E (1.8 Gbps)"
          ),
          DeviceEntity(
            id = "DEV-02",
            name = "Studio Workstation PC",
            type = "DESKTOP",
            platform = "Windows 11 Pro",
            isOnline = true,
            isTrusted = true,
            lastSeen = "Active 4m ago",
            connectionType = "Subnet LAN (Gigabit)"
          ),
          DeviceEntity(
            id = "DEV-03",
            name = "Sarah's iPad Pro 13\"",
            type = "TABLET",
            platform = "iPadOS 18",
            isOnline = true,
            isTrusted = true,
            lastSeen = "Active now",
            connectionType = "P2P Bluetooth 5.3 + Wi-Fi"
          ),
          DeviceEntity(
            id = "DEV-04",
            name = "Home Synology NAS",
            type = "SERVER",
            platform = "Linux DSM",
            isOnline = false,
            isTrusted = true,
            lastSeen = "2 hours ago",
            connectionType = "Encrypted Local WebDAV"
          ),
          DeviceEntity(
            id = "DEV-05",
            name = "Pixel 8a (Backup)",
            type = "PHONE",
            platform = "Android 15",
            isOnline = false,
            isTrusted = false,
            lastSeen = "Yesterday",
            connectionType = "Wi-Fi Direct"
          )
        )
      )
    }

    val existingContacts = contactDao.getAllContacts().first()
    if (existingContacts.isEmpty()) {
      contactDao.insertContacts(
        listOf(
          ContactEntity(
            id = "USR-01",
            name = "Sarah Chen",
            username = "sarahc",
            avatarInitials = "SC",
            deviceName = "MacBook Pro 16\"",
            isOnline = true,
            isVerified = true,
            isFavorite = true,
            avatarColorHex = 0xFF6366F1
          ),
          ContactEntity(
            id = "USR-02",
            name = "Alex Rivera",
            username = "arivera",
            avatarInitials = "AR",
            deviceName = "Studio Workstation PC",
            isOnline = true,
            isVerified = true,
            isFavorite = true,
            avatarColorHex = 0xFF0EA5E9
          ),
          ContactEntity(
            id = "USR-03",
            name = "David Kim",
            username = "dkim",
            avatarInitials = "DK",
            deviceName = "Galaxy Z Fold 6",
            isOnline = false,
            isVerified = true,
            isFavorite = false,
            avatarColorHex = 0xFF10B981
          ),
          ContactEntity(
            id = "USR-04",
            name = "Elena Rostova",
            username = "elena_r",
            avatarInitials = "ER",
            deviceName = "ThinkPad X1 Carbon",
            isOnline = true,
            isVerified = false,
            isFavorite = false,
            avatarColorHex = 0xFFF59E0B
          ),
          ContactEntity(
            id = "USR-05",
            name = "Omar Al-Mansoor",
            username = "omar_m",
            avatarInitials = "OA",
            deviceName = "Ubuntu Server Lab",
            isOnline = false,
            isVerified = true,
            isFavorite = false,
            avatarColorHex = 0xFF8B5CF6
          )
        )
      )
    }

    val existingTransfers = transferDao.getAllTransfers().first()
    if (existingTransfers.isEmpty()) {
      val now = System.currentTimeMillis()
      transferDao.insertTransfer(
        TransferEntity(
          id = "TRX-84192",
          direction = "SEND",
          senderName = "Mohamed Ahmed",
          senderUsername = "moha",
          senderDevice = "Pixel 9 Pro",
          receiverName = "Sarah Chen",
          receiverUsername = "sarahc",
          receiverDevice = "MacBook Pro 16\"",
          totalFiles = 3,
          totalBytes = 943_718_400L, // ~900 MB
          transferredBytes = 943_718_400L,
          speed = "28.4 MB/s",
          status = "COMPLETED",
          checksum = "SHA256:7d8a9f210b3e4f...9c8a",
          securityStatus = "AES-256-GCM Verified",
          createdAt = now - 3600_000L * 2,
          completedAt = now - 3600_000L * 2 + 35_000L,
          fileNamesSummary = "Product_Launch_4K.mov, Brand_Guidelines.pdf, Assets.zip",
          primaryFileType = "VIDEO"
        )
      )

      transferDao.insertTransfer(
        TransferEntity(
          id = "TRX-73201",
          direction = "RECEIVE",
          senderName = "Alex Rivera",
          senderUsername = "arivera",
          senderDevice = "Studio Workstation PC",
          receiverName = "Mohamed Ahmed",
          receiverUsername = "moha",
          receiverDevice = "Pixel 9 Pro",
          totalFiles = 1,
          totalBytes = 48_234_496L, // ~46 MB
          transferredBytes = 48_234_496L,
          speed = "34.1 MB/s",
          status = "COMPLETED",
          checksum = "SHA256:fa23910cbe449...11ad",
          securityStatus = "AES-256-GCM Verified",
          createdAt = now - 3600_000L * 5,
          completedAt = now - 3600_000L * 5 + 2_000L,
          fileNamesSummary = "Quarterly_Financial_Report_Q3.xlsx",
          primaryFileType = "DOCUMENT"
        )
      )

      transferDao.insertTransfer(
        TransferEntity(
          id = "TRX-61048",
          direction = "RECEIVE",
          senderName = "Elena Rostova",
          senderUsername = "elena_r",
          senderDevice = "ThinkPad X1 Carbon",
          receiverName = "Mohamed Ahmed",
          receiverUsername = "moha",
          receiverDevice = "Pixel 9 Pro",
          totalFiles = 8,
          totalBytes = 148_897_792L, // ~142 MB
          transferredBytes = 148_897_792L,
          speed = "19.8 MB/s",
          status = "COMPLETED",
          checksum = "SHA256:1a823b7849cc0...e582",
          securityStatus = "AES-256-GCM Verified",
          createdAt = now - 3600_000L * 24,
          completedAt = now - 3600_000L * 24 + 8_500L,
          fileNamesSummary = "Mobile_App_Figma_Exports_Batch (8 files)",
          primaryFileType = "IMAGE",
          failureReason = null
        )
      )

      transferDao.insertTransfer(
        TransferEntity(
          id = "TRX-FAILED-01",
          direction = "SEND",
          senderName = "Mohamed Ahmed",
          senderUsername = "moha",
          senderDevice = "Pixel 9 Pro",
          receiverName = "Omar Al-Mansoor",
          receiverUsername = "omar_m",
          receiverDevice = "Ubuntu Server Lab",
          totalFiles = 1,
          totalBytes = 1_887_436_800L, // ~1.8 GB
          transferredBytes = 320_000_000L,
          speed = "0.0 MB/s",
          status = "FAILED",
          checksum = "UNVERIFIED",
          securityStatus = "Handshake Terminated",
          createdAt = now - 3600_000L * 12,
          completedAt = null,
          fileNamesSummary = "Ubuntu_24.04_LTS_Server_Image.iso",
          primaryFileType = "ARCHIVE",
          failureReason = "Wi-Fi Direct P2P handshake timeout: Remote socket closed unexpectedly"
        )
      )
    }

    val existingMessages = messageDao.getAllMessages().first()
    if (existingMessages.isEmpty()) {
      val now = System.currentTimeMillis()
      messageDao.insertMessages(
        listOf(
          MessageEntity(
            id = "MSG-01",
            contactId = "USR-01",
            isMe = false,
            senderName = "Sarah Chen",
            content = "Hey Mohamed! Can you send over the updated launch cut?",
            isTransferCard = false,
            timestamp = now - 3600_000L * 3
          ),
          MessageEntity(
            id = "MSG-02",
            contactId = "USR-01",
            isMe = true,
            senderName = "Mohamed",
            content = "Sure thing, just transferred the 4K render and brand specs!",
            isTransferCard = false,
            timestamp = now - 3600_000L * 2
          ),
          MessageEntity(
            id = "MSG-03",
            contactId = "USR-01",
            isMe = true,
            senderName = "Mohamed",
            content = "Direct encrypted file package:",
            isTransferCard = true,
            transferFileName = "Product_Launch_4K.mov",
            transferFileSize = "900 MB",
            timestamp = now - 3600_000L * 2
          ),
          MessageEntity(
            id = "MSG-04",
            contactId = "USR-01",
            isMe = false,
            senderName = "Sarah Chen",
            content = "Received with 100% integrity check! Super fast transfer.",
            isTransferCard = false,
            timestamp = now - 3600_000L * 1
          )
        )
      )
    }

    val existingNotifs = notificationDao.getAllNotifications().first()
    if (existingNotifs.isEmpty()) {
      val now = System.currentTimeMillis()
      notificationDao.insertNotifications(
        listOf(
          NotificationEntity(
            id = "NOTIF-01",
            title = "Transfer Completed Successfully",
            body = "Product_Launch_4K.mov (900 MB) delivered to Sarah Chen with verified checksum.",
            type = "TRANSFER_COMPLETED",
            timestamp = now - 3600_000L * 2,
            isRead = false
          ),
          NotificationEntity(
            id = "NOTIF-02",
            title = "Device Connected: MacBook Pro 16\"",
            body = "Established high-speed local P2P encrypted channel via Wi-Fi 6E.",
            type = "DEVICE_CONNECTED",
            timestamp = now - 3600_000L * 4,
            isRead = false
          ),
          NotificationEntity(
            id = "NOTIF-03",
            title = "Security Handshake Verified",
            body = "End-to-End AES-256 session keys generated and validated.",
            type = "SECURITY",
            timestamp = now - 3600_000L * 8,
            isRead = true
          )
        )
      )
    }
  }

  // Transfer CRUD
  suspend fun insertTransfer(transfer: TransferEntity) = transferDao.insertTransfer(transfer)
  suspend fun updateTransfer(transfer: TransferEntity) = transferDao.updateTransfer(transfer)
  suspend fun updateTransferStatus(id: String, status: String) = transferDao.updateStatus(id, status)
  suspend fun logCompletedTransfer(id: String) = transferDao.updateTransferResult(
    id = id,
    status = "COMPLETED",
    completedAt = System.currentTimeMillis(),
    failureReason = null
  )
  suspend fun logFailedTransfer(id: String, reason: String) = transferDao.updateTransferResult(
    id = id,
    status = "FAILED",
    completedAt = null,
    failureReason = reason
  )
  suspend fun deleteTransfer(id: String) = transferDao.deleteTransferById(id)
  suspend fun clearHistory() = transferDao.clearAllTransfers()
  fun getTransferById(id: String): Flow<TransferEntity?> = transferDao.getTransferById(id)

  // Files
  suspend fun insertTransferFiles(files: List<TransferFileEntity>) = transferDao.insertTransferFiles(files)
  fun getFilesForTransfer(transferId: String): Flow<List<TransferFileEntity>> = transferDao.getFilesForTransfer(transferId)

  // Devices
  suspend fun updateDevice(device: DeviceEntity) = deviceDao.updateDevice(device)
  suspend fun insertDevice(device: DeviceEntity) = deviceDao.insertDevice(device)
  suspend fun deleteDevice(id: String) = deviceDao.deleteDevice(id)

  // Contacts
  suspend fun updateContact(contact: ContactEntity) = contactDao.updateContact(contact)
  suspend fun insertContact(contact: ContactEntity) = contactDao.insertContact(contact)

  // Messages
  fun getMessagesForContact(contactId: String) = messageDao.getMessagesForContact(contactId)
  suspend fun insertMessage(message: MessageEntity) = messageDao.insertMessage(message)

  // Notifications
  suspend fun insertNotification(notification: NotificationEntity) = notificationDao.insertNotification(notification)
  suspend fun markAllNotificationsRead() = notificationDao.markAllAsRead()
  suspend fun clearNotifications() = notificationDao.clearAllNotifications()
}
