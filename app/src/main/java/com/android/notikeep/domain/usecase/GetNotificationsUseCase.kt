package com.android.notikeep.domain.usecase

import androidx.paging.PagingData
import com.android.notikeep.domain.model.AppGroupSummary
import com.android.notikeep.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(category: String?): Flow<PagingData<AppGroupSummary>> =
        repository.getAppGroups(category)
}
