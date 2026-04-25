package com.android.notikeep.domain.repository

import com.android.notikeep.domain.model.AppNotification
import com.android.notikeep.domain.model.AppGroupSummary
import com.android.notikeep.domain.model.ConversationGroupSummary
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getAppGroups(category: String?): Flow<PagingData<AppGroupSummary>>
    fun getConversationGroupsByApp(packageName: String): Flow<PagingData<ConversationGroupSummary>>
    fun getNotificationsByConversation(
        packageName: String,
        conversationKey: String
    ): Flow<PagingData<AppNotification>>
    fun getLatestAppName(packageName: String): Flow<String?>
    suspend fun markAppAsRead(packageName: String)
    suspend fun saveNotification(notification: AppNotification)
    suspend fun deleteNotification(notification: AppNotification)
}
