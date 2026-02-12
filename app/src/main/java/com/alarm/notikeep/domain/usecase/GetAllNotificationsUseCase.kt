package com.alarm.notikeep.domain.usecase

import com.alarm.notikeep.domain.model.NotificationItem
import com.alarm.notikeep.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(): Flow<List<NotificationItem>> {
        return repository.getAllNotifications()
    }
}
