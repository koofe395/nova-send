package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.NovaDatabase
import com.example.data.model.ContactEntity
import com.example.data.model.DeviceEntity
import com.example.data.model.MessageEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.SelectedFileItem
import com.example.data.model.TransferEntity
import com.example.data.repository.NovaRepository
import com.example.engine.ActiveTransferProgress
import com.example.engine.EngineState
import com.example.engine.IncomingTransfer
import com.example.engine.TransferEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class NovaViewModel(application: Application) : AndroidViewModel(application) {
  private val database = NovaDatabase.getDatabase(application)
  val repository = NovaRepository(
    transferDao = database.transferDao(),
    deviceDao = database.deviceDao(),
    contactDao = database.contactDao(),
    messageDao = database.messageDao(),
    notificationDao = database.notificationDao(),
  )

  val transferEngine = TransferEngine(repository, viewModelScope)

  val allTransfers: StateFlow<List<TransferEntity>> = repository.allTransfers.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  val successfulTransfers: StateFlow<List<TransferEntity>> = repository.successfulTransfers.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  val failedTransfers: StateFlow<List<TransferEntity>> = repository.failedTransfers.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  val recentActivity: StateFlow<List<TransferEntity>> = repository.recentActivity.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  val allDevices: StateFlow<List<DeviceEntity>> = repository.allDevices.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  val allContacts: StateFlow<List<ContactEntity>> = repository.allContacts.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  val allNotifications: StateFlow<List<NotificationEntity>> = repository.allNotifications.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  val unreadNotificationsCount: StateFlow<Int> = repository.unreadNotifications.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = 0
  )

  val activeProgress: StateFlow<ActiveTransferProgress> = transferEngine.activeProgress
  val incomingTransfers: StateFlow<List<IncomingTransfer>> = transferEngine.incomingTransfers

  // Send Workflow State
  private val _sendStep = MutableStateFlow(1) // 1: Content, 2: Recipient, 3: Confirm, 4: Active Transfer
  val sendStep: StateFlow<Int> = _sendStep.asStateFlow()

  private val _selectedFiles = MutableStateFlow<List<SelectedFileItem>>(
    listOf(
      SelectedFileItem(
        id = "f1",
        name = "Quarterly_Financial_Report.pdf",
        sizeBytes = 14_800_000L,
        type = "DOCUMENT",
        extension = "pdf"
      ),
      SelectedFileItem(
        id = "f2",
        name = "Hero_Banner_Motion.mp4",
        sizeBytes = 420_000_000L,
        type = "VIDEO",
        extension = "mp4"
      )
    )
  )
  val selectedFiles: StateFlow<List<SelectedFileItem>> = _selectedFiles.asStateFlow()

  private val _selectedRecipient = MutableStateFlow<ContactEntity?>(null)
  val selectedRecipient: StateFlow<ContactEntity?> = _selectedRecipient.asStateFlow()

  // Selected Transfer for Details Dialog
  private val _inspectedTransfer = MutableStateFlow<TransferEntity?>(null)
  val inspectedTransfer: StateFlow<TransferEntity?> = _inspectedTransfer.asStateFlow()

  // Active Chat State
  private val _activeChatContact = MutableStateFlow<ContactEntity?>(null)
  val activeChatContact: StateFlow<ContactEntity?> = _activeChatContact.asStateFlow()

  private val _chatMessages = MutableStateFlow<List<MessageEntity>>(emptyList())
  val chatMessages: StateFlow<List<MessageEntity>> = _chatMessages.asStateFlow()

  // History Search & Filter State
  private val _historySearch = MutableStateFlow("")
  val historySearch: StateFlow<String> = _historySearch.asStateFlow()

  private val _historyFilter = MutableStateFlow("ALL") // ALL, SENT, RECEIVED, COMPLETED
  val historyFilter: StateFlow<String> = _historyFilter.asStateFlow()

  val filteredTransfers: StateFlow<List<TransferEntity>> = combine(
    allTransfers,
    _historySearch,
    _historyFilter
  ) { list, query, filter ->
    list.filter { item ->
      val matchesQuery = query.isBlank() ||
        item.fileNamesSummary.contains(query, ignoreCase = true) ||
        item.senderName.contains(query, ignoreCase = true) ||
        item.receiverName.contains(query, ignoreCase = true)

      val matchesFilter = when (filter) {
        "SENT" -> item.direction == "SEND"
        "RECEIVED" -> item.direction == "RECEIVE"
        "SUCCESSFUL", "COMPLETED" -> item.status == "COMPLETED"
        "FAILED" -> item.status in listOf("FAILED", "CANCELLED")
        "ACTIVE" -> item.status in listOf("TRANSFERRING", "CONNECTING", "VERIFYING", "WAITING_ACCEPTANCE", "ACCEPTED")
        else -> true
      }
      matchesQuery && matchesFilter
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Send Workflow Actions
  fun setSendStep(step: Int) {
    _sendStep.value = step.coerceIn(1, 4)
  }

  fun selectFile(file: SelectedFileItem) {
    if (_selectedFiles.value.none { it.id == file.id }) {
      _selectedFiles.value = _selectedFiles.value + file
    }
  }

  fun removeFile(fileId: String) {
    _selectedFiles.value = _selectedFiles.value.filter { it.id != fileId }
  }

  fun clearSelectedFiles() {
    _selectedFiles.value = emptyList()
  }

  fun addCustomFile(name: String, sizeBytes: Long, type: String) {
    val ext = name.substringAfterLast(".", "bin")
    val item = SelectedFileItem(
      id = UUID.randomUUID().toString(),
      name = name,
      sizeBytes = sizeBytes,
      type = type,
      extension = ext,
      isCustom = true
    )
    _selectedFiles.value = _selectedFiles.value + item
  }

  fun selectRecipient(contact: ContactEntity) {
    _selectedRecipient.value = contact
  }

  fun startSending() {
    val recipient = _selectedRecipient.value ?: return
    val files = _selectedFiles.value
    if (files.isEmpty()) return

    _sendStep.value = 4
    transferEngine.startSendTransfer(
      files = files,
      recipientName = recipient.name,
      recipientUsername = recipient.username,
      recipientDevice = recipient.deviceName
    )
  }

  fun cancelActiveTransfer() {
    transferEngine.cancelTransfer()
  }

  fun togglePauseTransfer() {
    transferEngine.togglePause()
  }

  fun resetSendFlow() {
    transferEngine.resetActiveTransfer()
    _sendStep.value = 1
  }

  // Receive Actions
  fun acceptIncoming(incoming: IncomingTransfer) {
    transferEngine.acceptIncomingTransfer(incoming)
  }

  fun declineIncoming(incoming: IncomingTransfer) {
    transferEngine.declineIncomingTransfer(incoming)
  }

  fun triggerSimulateIncoming() {
    transferEngine.triggerSimulatedIncomingTransfer()
  }

  // Details
  fun inspectTransfer(transfer: TransferEntity?) {
    _inspectedTransfer.value = transfer
  }

  fun deleteTransfer(id: String) {
    viewModelScope.launch {
      repository.deleteTransfer(id)
      if (_inspectedTransfer.value?.id == id) {
        _inspectedTransfer.value = null
      }
    }
  }

  fun clearAllHistory() {
    viewModelScope.launch {
      repository.clearHistory()
    }
  }

  // Devices Actions
  fun toggleDeviceTrust(device: DeviceEntity) {
    viewModelScope.launch {
      repository.updateDevice(device.copy(isTrusted = !device.isTrusted))
    }
  }

  fun toggleDeviceOnline(device: DeviceEntity) {
    viewModelScope.launch {
      repository.updateDevice(device.copy(isOnline = !device.isOnline))
    }
  }

  fun pairNewDevice(name: String, type: String, platform: String) {
    viewModelScope.launch {
      val newDev = DeviceEntity(
        id = "DEV-${UUID.randomUUID().toString().take(6).uppercase()}",
        name = name,
        type = type,
        platform = platform,
        isOnline = true,
        isTrusted = true,
        lastSeen = "Just now",
        connectionType = "Wi-Fi Direct P2P"
      )
      repository.insertDevice(newDev)
    }
  }

  // Chat Actions
  fun openChatWith(contact: ContactEntity) {
    _activeChatContact.value = contact
    viewModelScope.launch {
      repository.getMessagesForContact(contact.id).collect { msgs ->
        _chatMessages.value = msgs
      }
    }
  }

  fun closeChat() {
    _activeChatContact.value = null
  }

  fun sendMessage(text: String, isTransferCard: Boolean = false, fileName: String? = null, fileSize: String? = null) {
    val contact = _activeChatContact.value ?: return
    if (text.isBlank() && fileName == null) return

    viewModelScope.launch {
      val message = MessageEntity(
        id = "MSG-${System.currentTimeMillis()}",
        contactId = contact.id,
        isMe = true,
        senderName = "Mohamed",
        content = text,
        isTransferCard = isTransferCard,
        transferFileName = fileName,
        transferFileSize = fileSize,
        timestamp = System.currentTimeMillis()
      )
      repository.insertMessage(message)
    }
  }

  // Notifications
  fun markNotificationsRead() {
    viewModelScope.launch {
      repository.markAllNotificationsRead()
    }
  }

  fun clearAllNotifications() {
    viewModelScope.launch {
      repository.clearNotifications()
    }
  }

  // History Filter
  fun setHistorySearch(query: String) {
    _historySearch.value = query
  }

  fun setHistoryFilter(filter: String) {
    _historyFilter.value = filter
  }

  // Room Transfer Management (Success & Failure Logging)
  fun simulateTransferFailure(reason: String = "Wi-Fi Direct handshake socket reset by peer") {
    transferEngine.simulateFailure(reason)
  }

  fun retryTransfer(transfer: TransferEntity) {
    viewModelScope.launch {
      // Re-queue transfer in Room database
      val updated = transfer.copy(
        status = "CONNECTING",
        failureReason = null,
        speed = "Connecting...",
        transferredBytes = 0L,
        completedAt = null
      )
      repository.updateTransfer(updated)

      // Re-trigger simulated transfer through engine
      val files = listOf(
        SelectedFileItem(
          id = "retry_${transfer.id}",
          name = transfer.fileNamesSummary,
          sizeBytes = transfer.totalBytes,
          type = transfer.primaryFileType,
          extension = transfer.fileNamesSummary.substringAfterLast(".", "bin")
        )
      )
      transferEngine.startSendTransfer(
        files = files,
        recipientName = transfer.receiverName,
        recipientUsername = transfer.receiverUsername,
        recipientDevice = transfer.receiverDevice
      )
    }
  }

  // QR Code Generation & Pairing
  val myDeviceQrPayload: String by lazy {
    """{"app":"NOVA-SEND","v":"3.1","id":"DEV-9104","name":"Mohamed's Pixel 9 Pro","type":"PHONE","platform":"Android 15 (AP3A)","ip":"192.168.1.145","port":8848,"aesKey":"7F9B-2E81-C3A5","proto":"WIFI_DIRECT_5G"}"""
  }

  fun pairDeviceFromQr(payload: String): Pair<Boolean, String> {
    return try {
      // Parse payload or handle raw tokens
      val name = when {
        payload.contains("\"name\":") -> {
          val start = payload.indexOf("\"name\":") + 7
          val quoteStart = payload.indexOf("\"", start) + 1
          val quoteEnd = payload.indexOf("\"", quoteStart)
          payload.substring(quoteStart, quoteEnd)
        }
        payload.startsWith("DEV:") -> payload.substringAfter("DEV:").substringBefore(";")
        else -> "Discovered Peer Device (${payload.take(8)})"
      }

      val platform = when {
        payload.contains("macOS", ignoreCase = true) || payload.contains("MacBook", ignoreCase = true) -> "macOS Sequoia"
        payload.contains("Windows", ignoreCase = true) || payload.contains("PC", ignoreCase = true) -> "Windows 11 Pro"
        payload.contains("iPad", ignoreCase = true) || payload.contains("iOS", ignoreCase = true) -> "iPadOS 18"
        payload.contains("Linux", ignoreCase = true) || payload.contains("Ubuntu", ignoreCase = true) -> "Ubuntu Linux 24.04"
        else -> "Android 15"
      }

      val type = when {
        platform.contains("macOS", ignoreCase = true) -> "LAPTOP"
        platform.contains("Windows", ignoreCase = true) -> "DESKTOP"
        platform.contains("iPad", ignoreCase = true) -> "TABLET"
        platform.contains("Ubuntu", ignoreCase = true) -> "SERVER"
        else -> "PHONE"
      }

      val newDevice = DeviceEntity(
        id = "DEV-${UUID.randomUUID().toString().take(6).uppercase()}",
        name = name,
        type = type,
        platform = platform,
        isOnline = true,
        isTrusted = true,
        lastSeen = "Paired via QR Code",
        connectionType = "Direct P2P (Wi-Fi 6E AES-256)"
      )

      viewModelScope.launch {
        repository.insertDevice(newDevice)
        repository.insertNotification(
          NotificationEntity(
            id = "NOTIF-${System.currentTimeMillis()}",
            title = "New Device Paired via QR",
            body = "Secure authenticated link established with $name.",
            type = "DEVICE_CONNECTED",
            timestamp = System.currentTimeMillis(),
            isRead = false
          )
        )
      }
      Pair(true, "Successfully paired with $name!")
    } catch (e: Exception) {
      Pair(false, "Invalid NOVA-SEND QR Code payload.")
    }
  }
}
