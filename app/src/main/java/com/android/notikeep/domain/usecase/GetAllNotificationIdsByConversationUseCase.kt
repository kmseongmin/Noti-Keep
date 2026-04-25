package com.android.notikeep.domain.usecase

import com.android.notikeep.domain.repository.NotificationRepository
import javax.inject.Inject

class GetAllNotificationIdsByConversationUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(packageName: String, conversationKey: String): List<Long> =
        repository.getAllNotificationIdsByConversation(packageName, conversationKey)
}
