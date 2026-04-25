package com.android.notikeep.domain.usecase

import com.android.notikeep.domain.repository.NotificationRepository
import javax.inject.Inject

class GetAllConversationKeysByAppUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(packageName: String): List<String> =
        repository.getAllConversationKeysByApp(packageName)
}
