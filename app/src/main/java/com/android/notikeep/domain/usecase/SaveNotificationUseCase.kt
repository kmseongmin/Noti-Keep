package com.android.notikeep.domain.usecase

import com.android.notikeep.domain.model.AppNotification
import com.android.notikeep.domain.repository.NotificationRepository
import javax.inject.Inject

class SaveNotificationUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(notification: AppNotification) =
        repository.saveNotification(notification)
}
