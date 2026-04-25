package com.android.notikeep.domain.usecase

import com.android.notikeep.domain.repository.NotificationRepository
import javax.inject.Inject

class DeleteNotificationsByConversationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(packageName: String, conversationKeys: List<String>) =
        repository.deleteNotificationsByConversations(packageName, conversationKeys)
}
