package com.alarm.notikeep.data.repository

import com.alarm.notikeep.data.local.dao.NotificationDao
import com.alarm.notikeep.data.local.entity.NotificationEntity
import com.alarm.notikeep.domain.model.NotificationItem
import com.alarm.notikeep.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao
) : NotificationRepository {

    override suspend fun saveNotification(notification: NotificationItem) {
        notificationDao.insert(notification.toEntity())
    }

    override fun getAllNotifications(): Flow<List<NotificationItem>> {
        return notificationDao.getAllNotifications().map { entities ->
            entities.map { it.toModel() }
        }
    }

    override suspend fun getNotificationById(id: Long): NotificationItem? {
        return notificationDao.getNotificationById(id)?.toModel()
    }

    override fun getNotificationsByPackage(packageName: String): Flow<List<NotificationItem>> {
        return notificationDao.getNotificationsByPackage(packageName).map { entities ->
            entities.map { it.toModel() }
        }
    }

    override suspend fun deleteNotification(id: Long) {
        notificationDao.deleteById(id)
    }

    override suspend fun deleteAllNotifications() {
        notificationDao.deleteAll()
    }

    private fun NotificationEntity.toModel() = NotificationItem(
        id = id,
        packageName = packageName,
        appName = appName,
        title = title,
        content = content,
        timestamp = timestamp,
        iconData = iconData
    )

    private fun NotificationItem.toEntity() = NotificationEntity(
        id = id,
        packageName = packageName,
        appName = appName,
        title = title,
        content = content,
        timestamp = timestamp,
        iconData = iconData
    )
}
