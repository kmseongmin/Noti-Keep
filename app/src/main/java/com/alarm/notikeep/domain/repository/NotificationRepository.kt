package com.alarm.notikeep.domain.repository

import com.alarm.notikeep.domain.model.NotificationItem
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    suspend fun saveNotification(notification: NotificationItem)
    fun getAllNotifications(): Flow<List<NotificationItem>>
    suspend fun getNotificationById(id: Long): NotificationItem?
    fun getNotificationsByPackage(packageName: String): Flow<List<NotificationItem>>
    suspend fun deleteNotification(id: Long)
    suspend fun deleteAllNotifications()
}
