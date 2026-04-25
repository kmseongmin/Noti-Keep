package com.android.notikeep.data.repository

import com.android.notikeep.data.local.dao.NotificationDao
import com.android.notikeep.data.local.entity.NotificationEntity
import com.android.notikeep.domain.model.AppNotification
import com.android.notikeep.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val dao: NotificationDao
) : NotificationRepository {

    override fun getNotifications(): Flow<List<AppNotification>> =
        dao.getNotifications().map { list -> list.map { it.toDomain() } }

    override fun getNotificationsByApp(packageName: String): Flow<List<AppNotification>> =
        dao.getNotificationsByApp(packageName).map { list -> list.map { it.toDomain() } }

    override fun getNotificationsByConversation(packageName: String, conversationKey: String): Flow<List<AppNotification>> =
        dao.getNotificationsByConversation(packageName, conversationKey).map { list -> list.map { it.toDomain() } }

    override suspend fun saveNotification(notification: AppNotification) =
        dao.insertNotification(NotificationEntity.fromDomain(notification))

    override suspend fun deleteNotification(notification: AppNotification) =
        dao.deleteNotification(NotificationEntity.fromDomain(notification))
}
