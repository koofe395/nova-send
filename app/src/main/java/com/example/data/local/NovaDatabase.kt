package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ContactEntity
import com.example.data.model.DeviceEntity
import com.example.data.model.MessageEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.TransferEntity
import com.example.data.model.TransferFileEntity

@Database(
  entities = [
    TransferEntity::class,
    TransferFileEntity::class,
    DeviceEntity::class,
    ContactEntity::class,
    MessageEntity::class,
    NotificationEntity::class,
  ],
  version = 2,
  exportSchema = false,
)
abstract class NovaDatabase : RoomDatabase() {
  abstract fun transferDao(): TransferDao
  abstract fun deviceDao(): DeviceDao
  abstract fun contactDao(): ContactDao
  abstract fun messageDao(): MessageDao
  abstract fun notificationDao(): NotificationDao

  companion object {
    @Volatile
    private var INSTANCE: NovaDatabase? = null

    fun getDatabase(context: Context): NovaDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          NovaDatabase::class.java,
          "novasend_database"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
