package com.android.notikeep.domain.usecase

import androidx.paging.PagingData
import com.android.notikeep.domain.model.AppNotification
import com.android.notikeep.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotificationsByConversationUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(
        packageName: String,
        conversationKey: String
    ): Flow<PagingData<AppNotification>> =
        repository.getNotificationsByConversation(packageName, conversationKey)
}
