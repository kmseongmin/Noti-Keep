package com.android.notikeep.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.notikeep.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY receivedAt DESC")
    fun getNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE packageName = :packageName ORDER BY receivedAt DESC")
    fun getNotificationsByApp(packageName: String): Flow<List<NotificationEntity>>

    // subText가 있으면 subText(방 이름)로, 없으면 title(보낸 사람)로 조회
    @Query("""
        SELECT * FROM notifications
        WHERE packageName = :packageName
        AND (
            (subText IS NOT NULL AND subText = :conversationKey) OR
            (subText IS NULL AND title = :conversationKey)
        )
        ORDER BY receivedAt DESC
    """)
    fun getNotificationsByConversation(packageName: String, conversationKey: String): Flow<List<NotificationEntity>>

    @Query("UPDATE notifications SET isRead = 1 WHERE packageName = :packageName")
    suspend fun markAppAsRead(packageName: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)
}
