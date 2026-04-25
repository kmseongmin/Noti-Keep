package com.android.notikeep.data.repository

import com.android.notikeep.data.local.dao.NotificationDao
import com.android.notikeep.data.local.entity.NotificationEntity
import com.android.notikeep.domain.model.AppNotification
import com.android.notikeep.domain.model.AppGroupSummary
import com.android.notikeep.domain.model.ConversationGroupSummary
import com.android.notikeep.domain.repository.NotificationRepository
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val dao: NotificationDao
) : NotificationRepository {

    override fun getAppGroups(category: String?): Flow<PagingData<AppGroupSummary>> =
        Pager(
            config = PagingConfig(pageSize = 30, enablePlaceholders = false),
            pagingSourceFactory = { dao.getAppGroups(category) }
        ).flow.map { pagingData ->
            pagingData.map { row ->
                AppGroupSummary(
                    packageName = row.packageName,
                    appName = row.appName,
                    latestTitle = row.latestTitle,
                    latestContent = row.latestContent,
                    latestCategory = row.latestCategory,
                    latestReceivedAt = row.latestReceivedAt,
                    totalCount = row.totalCount,
                    unreadCount = row.unreadCount
                )
            }
        }

    override fun getConversationGroupsByApp(packageName: String): Flow<PagingData<ConversationGroupSummary>> =
        Pager(
            config = PagingConfig(pageSize = 30, enablePlaceholders = false),
            pagingSourceFactory = { dao.getConversationGroupsByApp(packageName) }
        ).flow.map { pagingData ->
            pagingData.map { row ->
                ConversationGroupSummary(
                    conversationKey = row.conversationKey,
                    packageName = row.packageName,
                    appName = row.appName,
                    latestTitle = row.latestTitle,
                    latestContent = row.latestContent,
                    latestSubText = row.latestSubText,
                    latestCategory = row.latestCategory,
                    latestReceivedAt = row.latestReceivedAt,
                    count = row.count,
                    unreadCount = row.unreadCount
                )
            }
        }

    override fun getNotificationsByConversation(
        packageName: String,
        conversationKey: String
    ): Flow<PagingData<AppNotification>> =
        Pager(
            config = PagingConfig(pageSize = 40, enablePlaceholders = false),
            pagingSourceFactory = { dao.getNotificationsByConversation(packageName, conversationKey) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }

    override fun getLatestAppName(packageName: String): Flow<String?> =
        dao.getLatestAppName(packageName)

    override suspend fun getAllPackageNames(category: String?): List<String> =
        dao.getAllPackageNames(category)

    override suspend fun getAllConversationKeysByApp(packageName: String): List<String> =
        dao.getAllConversationKeysByApp(packageName)

    override suspend fun getAllNotificationIdsByConversation(
        packageName: String,
        conversationKey: String
    ): List<Long> = dao.getAllNotificationIdsByConversation(packageName, conversationKey)

    override suspend fun markAppAsRead(packageName: String) =
        dao.markAppAsRead(packageName)

    override suspend fun markConversationAsRead(packageName: String, conversationKey: String) =
        dao.markConversationAsRead(packageName, conversationKey)

    override suspend fun deleteNotificationsByPackages(packageNames: List<String>) =
        dao.deleteNotificationsByPackages(packageNames)

    override suspend fun deleteNotificationsByConversations(
        packageName: String,
        conversationKeys: List<String>
    ) = dao.deleteNotificationsByConversations(packageName, conversationKeys)

    override suspend fun deleteNotificationsByIds(ids: List<Long>) =
        dao.deleteNotificationsByIds(ids)

    override suspend fun saveNotification(notification: AppNotification) =
        dao.insertNotification(NotificationEntity.fromDomain(notification))

    override suspend fun deleteNotification(notification: AppNotification) =
        dao.deleteNotification(NotificationEntity.fromDomain(notification))
}
