package com.android.notikeep.domain.usecase

import com.android.notikeep.domain.model.AppNotification
import com.android.notikeep.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(): Flow<List<AppNotification>> = repository.getNotifications()
}
