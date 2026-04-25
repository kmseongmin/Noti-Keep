package com.android.notikeep.domain.usecase

import com.android.notikeep.domain.repository.NotificationRepository
import javax.inject.Inject

class GetAllPackageNamesUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(category: String?): List<String> =
        repository.getAllPackageNames(category)
}
