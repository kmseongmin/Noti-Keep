package com.android.notikeep.domain.usecase

import com.android.notikeep.domain.repository.NotificationRepository
import javax.inject.Inject

class DeleteNotificationsByIdsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(ids: List<Long>) =
        repository.deleteNotificationsByIds(ids)
}
