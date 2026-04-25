package com.android.notikeep.domain.usecase

import com.android.notikeep.domain.model.AppNotification
import com.android.notikeep.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotificationsByAppUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(packageName: String): Flow<List<AppNotification>> =
        repository.getNotificationsByApp(packageName)
}
