package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ContactEntity
import com.example.data.model.DeviceEntity
import com.example.data.model.MessageEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.TransferEntity
import com.example.data.model.TransferFileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferDao {
  @Query("SELECT * FROM transfers ORDER BY createdAt DESC")
  fun getAllTransfers(): Flow<List<TransferEntity>>

  @Query("SELECT * FROM transfers WHERE status IN ('TRANSFERRING', 'CONNECTING', 'VERIFYING', 'WAITING_ACCEPTANCE', 'ACCEPTED') ORDER BY createdAt DESC")
  fun getActiveTransfers(): Flow<List<TransferEntity>>

  @Query("SELECT * FROM transfers WHERE id = :id")
  fun getTransferById(id: String): Flow<TransferEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransfer(transfer: TransferEntity)

  @Update
  suspend fun updateTransfer(transfer: TransferEntity)

  @Query("DELETE FROM transfers WHERE id = :id")
  suspend fun deleteTransferById(id: String)

  @Query("SELECT * FROM transfer_files WHERE transferId = :transferId")
  fun getFilesForTransfer(transferId: String): Flow<List<TransferFileEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransferFiles(files: List<TransferFileEntity>)

  @Update
  suspend fun updateTransferFile(file: TransferFileEntity)

  @Query("UPDATE transfers SET status = :status WHERE id = :id")
  suspend fun updateStatus(id: String, status: String)

  @Query("SELECT * FROM transfers WHERE status = 'COMPLETED' ORDER BY completedAt DESC, createdAt DESC")
  fun getSuccessfulTransfers(): Flow<List<TransferEntity>>

  @Query("SELECT * FROM transfers WHERE status IN ('FAILED', 'CANCELLED') ORDER BY createdAt DESC")
  fun getFailedTransfers(): Flow<List<TransferEntity>>

  @Query("SELECT * FROM transfers ORDER BY createdAt DESC LIMIT :limit")
  fun getRecentActivity(limit: Int = 30): Flow<List<TransferEntity>>

  @Query("UPDATE transfers SET status = :status, completedAt = :completedAt, failureReason = :failureReason WHERE id = :id")
  suspend fun updateTransferResult(id: String, status: String, completedAt: Long?, failureReason: String?)

  @Query("DELETE FROM transfers")
  suspend fun clearAllTransfers()
}

@Dao
interface DeviceDao {
  @Query("SELECT * FROM devices ORDER BY isOnline DESC, isTrusted DESC")
  fun getAllDevices(): Flow<List<DeviceEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDevices(devices: List<DeviceEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDevice(device: DeviceEntity)

  @Update
  suspend fun updateDevice(device: DeviceEntity)

  @Query("DELETE FROM devices WHERE id = :id")
  suspend fun deleteDevice(id: String)
}

@Dao
interface ContactDao {
  @Query("SELECT * FROM contacts ORDER BY isOnline DESC, isFavorite DESC, name ASC")
  fun getAllContacts(): Flow<List<ContactEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertContacts(contacts: List<ContactEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertContact(contact: ContactEntity)

  @Update
  suspend fun updateContact(contact: ContactEntity)

  @Query("DELETE FROM contacts WHERE id = :id")
  suspend fun deleteContact(id: String)
}

@Dao
interface MessageDao {
  @Query("SELECT * FROM messages WHERE contactId = :contactId ORDER BY timestamp ASC")
  fun getMessagesForContact(contactId: String): Flow<List<MessageEntity>>

  @Query("SELECT * FROM messages ORDER BY timestamp DESC")
  fun getAllMessages(): Flow<List<MessageEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMessages(messages: List<MessageEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMessage(message: MessageEntity)
}

@Dao
interface NotificationDao {
  @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
  fun getAllNotifications(): Flow<List<NotificationEntity>>

  @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
  fun getUnreadCount(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNotification(notification: NotificationEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNotifications(notifications: List<NotificationEntity>)

  @Query("UPDATE notifications SET isRead = 1")
  suspend fun markAllAsRead()

  @Query("DELETE FROM notifications")
  suspend fun clearAllNotifications()
}
