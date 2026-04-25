package com.android.notikeep.domain.usecase

import com.android.notikeep.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLatestAppNameUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(packageName: String): Flow<String?> =
        repository.getLatestAppName(packageName)
}
