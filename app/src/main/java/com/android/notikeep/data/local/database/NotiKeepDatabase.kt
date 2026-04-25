package com.android.notikeep.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.android.notikeep.data.local.dao.NotificationDao
import com.android.notikeep.data.local.entity.NotificationEntity

@Database(
    entities = [NotificationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NotiKeepDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
}
