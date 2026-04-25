package com.android.notikeep.domain.usecase

import com.android.notikeep.domain.repository.NotificationRepository
import javax.inject.Inject

class DeleteNotificationsByPackagesUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(packageNames: List<String>) =
        repository.deleteNotificationsByPackages(packageNames)
}
