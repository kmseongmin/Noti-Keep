package com.android.notikeep.domain.usecase

import androidx.paging.PagingData
import com.android.notikeep.domain.model.ConversationGroupSummary
import com.android.notikeep.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotificationsByAppUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(packageName: String): Flow<PagingData<ConversationGroupSummary>> =
        repository.getConversationGroupsByApp(packageName)
}
