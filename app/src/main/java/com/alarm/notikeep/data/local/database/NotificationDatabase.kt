package com.alarm.notikeep.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.alarm.notikeep.data.local.dao.NotificationDao
import com.alarm.notikeep.data.local.entity.NotificationEntity

@Database(
    entities = [NotificationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NotificationDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
}
