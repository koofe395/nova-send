package com.example.engine

import com.example.data.model.NotificationEntity
import com.example.data.model.SelectedFileItem
import com.example.data.model.TransferEntity
import com.example.data.model.TransferFileEntity
import com.example.data.repository.NovaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

enum class EngineState {
  IDLE,
  FILE_SELECTED,
  RECIPIENT_SELECTED,
  REQUEST_CREATED,
  WAITING_FOR_ACCEPTANCE,
  ACCEPTED,
  CONNECTING,
  TRANSFERRING,
  VERIFYING,
  COMPLETED,
  PAUSED,
  CANCELLED,
  FAILED
}

data class ActiveTransferProgress(
  val transferId: String = "",
  val direction: String = "SEND", // "SEND" or "RECEIVE"
  val partnerName: String = "",
  val partnerDevice: String = "",
  val partnerUsername: String = "",
  val state: EngineState = EngineState.IDLE,
  val overallProgress: Float = 0f, // 0.0 to 1.0
  val transferredBytes: Long = 0L,
  val totalBytes: Long = 0L,
  val speedMbPerSec: Float = 0f,
  val etaSeconds: Int = 0,
  val currentFileName: String = "",
  val currentFileIndex: Int = 0,
  val totalFiles: Int = 0,
  val filesProgress: List<TransferFileEntity> = emptyList(),
  val checksumHash: String = "",
  val securityProtocol: String = "AES-256-GCM Direct P2P",
  val statusMessage: String = "Ready",
)

data class IncomingTransfer(
  val id: String,
  val senderName: String,
  val senderUsername: String,
  val senderDevice: String,
  val avatarColorHex: Long,
  val totalFiles: Int,
  val totalBytes: Long,
  val filesSummary: String,
  val fileType: String,
  val securityGrade: String = "End-to-End Encrypted (SHA-256)",
)

class TransferEngine(
  private val repository: NovaRepository,
  private val engineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
  private val _activeProgress = MutableStateFlow(ActiveTransferProgress())
  val activeProgress: StateFlow<ActiveTransferProgress> = _activeProgress.asStateFlow()

  private val _incomingTransfers = MutableStateFlow<List<IncomingTransfer>>(
    listOf(
      IncomingTransfer(
        id = "INC-1092",
        senderName = "Sarah Chen",
        senderUsername = "sarahc",
        senderDevice = "MacBook Pro 16\"",
        avatarColorHex = 0xFF6366F1,
        totalFiles = 2,
        totalBytes = 895_485_952L, // ~854 MB
        filesSummary = "Figma_Design_Tokens_v2.zip, Preview_Board.png",
        fileType = "ARCHIVE"
      )
    )
  )
  val incomingTransfers: StateFlow<List<IncomingTransfer>> = _incomingTransfers.asStateFlow()

  private var transferJob: Job? = null
  private var isPaused = false

  fun startSendTransfer(
    files: List<SelectedFileItem>,
    recipientName: String,
    recipientUsername: String,
    recipientDevice: String
  ) {
    transferJob?.cancel()
    isPaused = false

    val transferId = "TRX-${Random.nextInt(10000, 99999)}"
    val totalBytes = files.sumOf { it.sizeBytes }
    val totalFiles = files.size
    val fileEntities = files.mapIndexed { index, file ->
      TransferFileEntity(
        id = UUID.randomUUID().toString(),
        transferId = transferId,
        fileName = file.name,
        fileSize = file.sizeBytes,
        fileType = file.type,
        progress = 0f,
        status = if (index == 0) "TRANSFERRING" else "QUEUED",
        checksum = "SHA256:${UUID.randomUUID().toString().take(12)}"
      )
    }

    _activeProgress.value = ActiveTransferProgress(
      transferId = transferId,
      direction = "SEND",
      partnerName = recipientName,
      partnerDevice = recipientDevice,
      partnerUsername = recipientUsername,
      state = EngineState.REQUEST_CREATED,
      overallProgress = 0f,
      transferredBytes = 0L,
      totalBytes = totalBytes,
      speedMbPerSec = 0f,
      etaSeconds = (totalBytes / (25 * 1024 * 1024L)).toInt().coerceAtLeast(2),
      currentFileName = files.firstOrNull()?.name ?: "",
      currentFileIndex = 1,
      totalFiles = totalFiles,
      filesProgress = fileEntities,
      checksumHash = "SHA256:${UUID.randomUUID().toString().replace("-", "").take(24)}",
      statusMessage = "Sending transfer request to $recipientName..."
    )

    transferJob = engineScope.launch {
      // 1. Request Created & Handshake
      delay(800)
      _activeProgress.value = _activeProgress.value.copy(
        state = EngineState.WAITING_FOR_ACCEPTANCE,
        statusMessage = "Waiting for $recipientName to accept..."
      )

      delay(1200)
      _activeProgress.value = _activeProgress.value.copy(
        state = EngineState.ACCEPTED,
        statusMessage = "$recipientName accepted! Establishing secure session..."
      )

      // 2. Connecting & Key Exchange
      delay(700)
      _activeProgress.value = _activeProgress.value.copy(
        state = EngineState.CONNECTING,
        statusMessage = "Exchanging TLS 1.3 keys & establishing Direct P2P tunnel..."
      )

      delay(900)
      _activeProgress.value = _activeProgress.value.copy(
        state = EngineState.TRANSFERRING,
        statusMessage = "Transferring data over encrypted stream..."
      )

      // 3. Simulated Chunk Streaming
      var currentBytes = 0L
      val chunkSize = (totalBytes / 40L).coerceAtLeast(1024 * 512L) // ~40 updates
      var fileIndex = 0

      while (isActive && currentBytes < totalBytes) {
        if (isPaused) {
          delay(200)
          continue
        }

        delay(120) // update tick
        val randomJitter = Random.nextLong(-chunkSize / 4, chunkSize / 4)
        val step = (chunkSize + randomJitter).coerceAtLeast(1024 * 128L)
        currentBytes = (currentBytes + step).coerceAtMost(totalBytes)

        val progressFrac = (currentBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
        val currentSpeed = Random.nextDouble(22.0, 36.5).toFloat()
        val remainingBytes = totalBytes - currentBytes
        val remainingSecs = if (currentSpeed > 0) {
          (remainingBytes / (currentSpeed * 1024 * 1024L)).toInt().coerceAtLeast(1)
        } else 1

        fileIndex = ((currentBytes.toFloat() / totalBytes.toFloat()) * totalFiles).toInt().coerceIn(0, totalFiles - 1)
        val updatedFiles = fileEntities.mapIndexed { idx, item ->
          when {
            idx < fileIndex -> item.copy(progress = 1f, status = "COMPLETED")
            idx == fileIndex -> {
              val fileProgress = ((progressFrac * totalFiles) - fileIndex).coerceIn(0f, 1f)
              item.copy(progress = fileProgress, status = "TRANSFERRING")
            }
            else -> item.copy(progress = 0f, status = "QUEUED")
          }
        }

        _activeProgress.value = _activeProgress.value.copy(
          overallProgress = progressFrac,
          transferredBytes = currentBytes,
          speedMbPerSec = currentSpeed,
          etaSeconds = remainingSecs,
          currentFileIndex = fileIndex + 1,
          currentFileName = files.getOrNull(fileIndex)?.name ?: "",
          filesProgress = updatedFiles,
          statusMessage = "Transferring at ${"%.1f".format(currentSpeed)} MB/s"
        )
      }

      // 4. Verifying
      _activeProgress.value = _activeProgress.value.copy(
        state = EngineState.VERIFYING,
        overallProgress = 0.99f,
        speedMbPerSec = 0f,
        statusMessage = "Calculating cryptographic SHA-256 integrity hash..."
      )
      delay(900)

      // 5. Completed
      val completedFiles = fileEntities.map { it.copy(progress = 1f, status = "COMPLETED") }
      _activeProgress.value = _activeProgress.value.copy(
        state = EngineState.COMPLETED,
        overallProgress = 1f,
        transferredBytes = totalBytes,
        speedMbPerSec = 0f,
        etaSeconds = 0,
        filesProgress = completedFiles,
        statusMessage = "Transfer complete! 100% integrity verified."
      )

      // Record to repository
      val now = System.currentTimeMillis()
      val transferRecord = TransferEntity(
        id = transferId,
        direction = "SEND",
        senderName = "Mohamed Ahmed",
        senderUsername = "moha",
        senderDevice = "Pixel 9 Pro",
        receiverName = recipientName,
        receiverUsername = recipientUsername,
        receiverDevice = recipientDevice,
        totalFiles = totalFiles,
        totalBytes = totalBytes,
        transferredBytes = totalBytes,
        speed = "31.2 MB/s",
        status = "COMPLETED",
        checksum = _activeProgress.value.checksumHash,
        securityStatus = "AES-256-GCM Verified",
        createdAt = now - 15_000L,
        completedAt = now,
        fileNamesSummary = files.joinToString(", ") { it.name },
        primaryFileType = files.firstOrNull()?.type ?: "MIXED"
      )
      repository.insertTransfer(transferRecord)
      repository.insertTransferFiles(completedFiles)

      repository.insertNotification(
        NotificationEntity(
          id = "NOTIF-${System.currentTimeMillis()}",
          title = "Transfer to $recipientName Complete",
          body = "${files.firstOrNull()?.name} (${files.size} items) delivered successfully.",
          type = "TRANSFER_COMPLETED",
          timestamp = now,
          isRead = false
        )
      )
    }
  }

  fun acceptIncomingTransfer(incoming: IncomingTransfer) {
    _incomingTransfers.value = _incomingTransfers.value.filter { it.id != incoming.id }
    transferJob?.cancel()
    isPaused = false

    val transferId = "TRX-${Random.nextInt(10000, 99999)}"
    val totalBytes = incoming.totalBytes
    val fileNames = incoming.filesSummary.split(", ")
    val fileEntities = fileNames.mapIndexed { index, name ->
      TransferFileEntity(
        id = UUID.randomUUID().toString(),
        transferId = transferId,
        fileName = name,
        fileSize = totalBytes / fileNames.size,
        fileType = incoming.fileType,
        progress = 0f,
        status = if (index == 0) "TRANSFERRING" else "QUEUED",
        checksum = "SHA256:${UUID.randomUUID().toString().take(12)}"
      )
    }

    _activeProgress.value = ActiveTransferProgress(
      transferId = transferId,
      direction = "RECEIVE",
      partnerName = incoming.senderName,
      partnerDevice = incoming.senderDevice,
      partnerUsername = incoming.senderUsername,
      state = EngineState.ACCEPTED,
      overallProgress = 0f,
      transferredBytes = 0L,
      totalBytes = totalBytes,
      speedMbPerSec = 0f,
      etaSeconds = (totalBytes / (30 * 1024 * 1024L)).toInt().coerceAtLeast(2),
      currentFileName = fileNames.firstOrNull() ?: "",
      currentFileIndex = 1,
      totalFiles = fileNames.size,
      filesProgress = fileEntities,
      checksumHash = "SHA256:${UUID.randomUUID().toString().replace("-", "").take(24)}",
      statusMessage = "Accepted transfer from ${incoming.senderName}. Connecting..."
    )

    transferJob = engineScope.launch {
      delay(600)
      _activeProgress.value = _activeProgress.value.copy(
        state = EngineState.CONNECTING,
        statusMessage = "Receiving cryptographic handshake..."
      )

      delay(700)
      _activeProgress.value = _activeProgress.value.copy(
        state = EngineState.TRANSFERRING,
        statusMessage = "Receiving file stream into local downloads cache..."
      )

      var currentBytes = 0L
      val chunkSize = (totalBytes / 35L).coerceAtLeast(1024 * 512L)
      var fileIndex = 0

      while (isActive && currentBytes < totalBytes) {
        if (isPaused) {
          delay(200)
          continue
        }

        delay(130)
        val randomJitter = Random.nextLong(-chunkSize / 4, chunkSize / 4)
        val step = (chunkSize + randomJitter).coerceAtLeast(1024 * 128L)
        currentBytes = (currentBytes + step).coerceAtMost(totalBytes)

        val progressFrac = (currentBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
        val currentSpeed = Random.nextDouble(28.0, 42.0).toFloat()
        val remainingBytes = totalBytes - currentBytes
        val remainingSecs = if (currentSpeed > 0) {
          (remainingBytes / (currentSpeed * 1024 * 1024L)).toInt().coerceAtLeast(1)
        } else 1

        fileIndex = ((currentBytes.toFloat() / totalBytes.toFloat()) * fileNames.size).toInt().coerceIn(0, fileNames.size - 1)
        val updatedFiles = fileEntities.mapIndexed { idx, item ->
          when {
            idx < fileIndex -> item.copy(progress = 1f, status = "COMPLETED")
            idx == fileIndex -> {
              val fileProgress = ((progressFrac * fileNames.size) - fileIndex).coerceIn(0f, 1f)
              item.copy(progress = fileProgress, status = "TRANSFERRING")
            }
            else -> item.copy(progress = 0f, status = "QUEUED")
          }
        }

        _activeProgress.value = _activeProgress.value.copy(
          overallProgress = progressFrac,
          transferredBytes = currentBytes,
          speedMbPerSec = currentSpeed,
          etaSeconds = remainingSecs,
          currentFileIndex = fileIndex + 1,
          currentFileName = fileNames.getOrNull(fileIndex) ?: "",
          filesProgress = updatedFiles,
          statusMessage = "Receiving at ${"%.1f".format(currentSpeed)} MB/s"
        )
      }

      // Verifying
      _activeProgress.value = _activeProgress.value.copy(
        state = EngineState.VERIFYING,
        overallProgress = 0.99f,
        speedMbPerSec = 0f,
        statusMessage = "Verifying incoming payload integrity & saving to disk..."
      )
      delay(800)

      // Completed
      val completedFiles = fileEntities.map { it.copy(progress = 1f, status = "COMPLETED") }
      _activeProgress.value = _activeProgress.value.copy(
        state = EngineState.COMPLETED,
        overallProgress = 1f,
        transferredBytes = totalBytes,
        speedMbPerSec = 0f,
        etaSeconds = 0,
        filesProgress = completedFiles,
        statusMessage = "File received and saved to /Downloads/NovaSend!"
      )

      val now = System.currentTimeMillis()
      val record = TransferEntity(
        id = transferId,
        direction = "RECEIVE",
        senderName = incoming.senderName,
        senderUsername = incoming.senderUsername,
        senderDevice = incoming.senderDevice,
        receiverName = "Mohamed Ahmed",
        receiverUsername = "moha",
        receiverDevice = "Pixel 9 Pro",
        totalFiles = incoming.totalFiles,
        totalBytes = totalBytes,
        transferredBytes = totalBytes,
        speed = "38.5 MB/s",
        status = "COMPLETED",
        checksum = _activeProgress.value.checksumHash,
        securityStatus = "AES-256-GCM Verified",
        createdAt = now - 12_000L,
        completedAt = now,
        fileNamesSummary = incoming.filesSummary,
        primaryFileType = incoming.fileType
      )
      repository.insertTransfer(record)
      repository.insertTransferFiles(completedFiles)

      repository.insertNotification(
        NotificationEntity(
          id = "NOTIF-${System.currentTimeMillis()}",
          title = "File Received from ${incoming.senderName}",
          body = "${incoming.filesSummary} saved to device safely.",
          type = "TRANSFER_COMPLETED",
          timestamp = now,
          isRead = false
        )
      )
    }
  }

  fun declineIncomingTransfer(incoming: IncomingTransfer) {
    _incomingTransfers.value = _incomingTransfers.value.filter { it.id != incoming.id }
    engineScope.launch {
      repository.insertNotification(
        NotificationEntity(
          id = "NOTIF-${System.currentTimeMillis()}",
          title = "Declined Transfer Request",
          body = "Incoming file offer from ${incoming.senderName} was declined.",
          type = "TRANSFER_REQUEST",
          timestamp = System.currentTimeMillis(),
          isRead = true
        )
      )
    }
  }

  fun triggerSimulatedIncomingTransfer() {
    val sampleSenders = listOf(
      Triple("Alex Rivera", "arivera", "Studio Workstation PC"),
      Triple("David Kim", "dkim", "Galaxy Z Fold 6"),
      Triple("Sarah Chen", "sarahc", "MacBook Pro 16\""),
      Triple("Omar Al-Mansoor", "omar_m", "Ubuntu Server Lab")
    )
    val chosen = sampleSenders.random()
    val sampleFiles = listOf(
      Pair("Project_Deck_Q4_Final.pdf", 28_450_000L),
      Pair("Raw_Cinema_Shot_042.mov", 740_200_000L),
      Pair("Neural_Weights_Checkpoint.bin", 320_000_000L),
      Pair("Studio_Stems_Audio_Mix.zip", 145_000_000L)
    ).random()

    val newIncoming = IncomingTransfer(
      id = "INC-${Random.nextInt(1000, 9999)}",
      senderName = chosen.first,
      senderUsername = chosen.second,
      senderDevice = chosen.third,
      avatarColorHex = 0xFF0EA5E9,
      totalFiles = 1,
      totalBytes = sampleFiles.second,
      filesSummary = sampleFiles.first,
      fileType = if (sampleFiles.first.endsWith(".mov")) "VIDEO" else if (sampleFiles.first.endsWith(".pdf")) "DOCUMENT" else "ARCHIVE"
    )

    _incomingTransfers.value = listOf(newIncoming) + _incomingTransfers.value
  }

  fun togglePause() {
    isPaused = !isPaused
    _activeProgress.value = _activeProgress.value.copy(
      state = if (isPaused) EngineState.PAUSED else EngineState.TRANSFERRING,
      statusMessage = if (isPaused) "Transfer paused by user" else "Resuming data stream..."
    )
  }

  fun cancelTransfer() {
    val current = _activeProgress.value
    transferJob?.cancel()
    isPaused = false
    _activeProgress.value = current.copy(
      state = EngineState.CANCELLED,
      statusMessage = "Transfer cancelled"
    )
    if (current.transferId.isNotEmpty()) {
      engineScope.launch {
        repository.logFailedTransfer(current.transferId, "Transfer cancelled by local user")
      }
    }
  }

  fun simulateFailure(reason: String = "Wi-Fi Direct P2P handshake timeout: Socket connection closed unexpectedly") {
    val current = _activeProgress.value
    if (current.state == EngineState.IDLE) return
    transferJob?.cancel()
    isPaused = false
    _activeProgress.value = current.copy(
      state = EngineState.FAILED,
      speedMbPerSec = 0f,
      statusMessage = "Failed: $reason"
    )
    val now = System.currentTimeMillis()
    engineScope.launch {
      val failedTransfer = TransferEntity(
        id = current.transferId.ifEmpty { "TRX-${Random.nextInt(10000, 99999)}" },
        direction = current.direction,
        senderName = if (current.direction == "SEND") "Mohamed Ahmed" else current.partnerName,
        senderUsername = if (current.direction == "SEND") "moha" else current.partnerUsername,
        senderDevice = if (current.direction == "SEND") "Pixel 9 Pro" else current.partnerDevice,
        receiverName = if (current.direction == "SEND") current.partnerName else "Mohamed Ahmed",
        receiverUsername = if (current.direction == "SEND") current.partnerUsername else "moha",
        receiverDevice = if (current.direction == "SEND") current.partnerDevice else "Pixel 9 Pro",
        totalFiles = current.totalFiles.coerceAtLeast(1),
        totalBytes = current.totalBytes,
        transferredBytes = current.transferredBytes,
        speed = "0.0 MB/s",
        status = "FAILED",
        checksum = "UNVERIFIED",
        securityStatus = "Interrupted",
        createdAt = now - 10000L,
        completedAt = null,
        fileNamesSummary = current.currentFileName.ifEmpty { "P2P_Data_Stream" },
        primaryFileType = "MIXED",
        failureReason = reason
      )
      repository.insertTransfer(failedTransfer)
      repository.insertNotification(
        NotificationEntity(
          id = "NOTIF-${System.currentTimeMillis()}",
          title = "Transfer Failed",
          body = "File transfer with ${current.partnerName} failed: $reason",
          type = "SECURITY",
          timestamp = now,
          isRead = false
        )
      )
    }
  }

  fun resetActiveTransfer() {
    transferJob?.cancel()
    isPaused = false
    _activeProgress.value = ActiveTransferProgress()
  }
}
