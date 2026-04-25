package com.android.notikeep.domain.repository

import com.android.notikeep.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(): Flow<List<AppNotification>>
    fun getNotificationsByApp(packageName: String): Flow<List<AppNotification>>
    fun getNotificationsByConversation(packageName: String, title: String): Flow<List<AppNotification>>
    suspend fun saveNotification(notification: AppNotification)
    suspend fun deleteNotification(notification: AppNotification)
}
