package com.android.notikeep.domain.usecase

import com.android.notikeep.domain.repository.NotificationRepository
import javax.inject.Inject

/**
 * 홈 화면 선택 삭제용: 선택된 앱 패키지들의 알림을 전부 삭제하는 UseCase.
 * HomeViewModel.deleteSelected() 에서 호출.
 */
class DeleteNotificationsByPackagesUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    /** @param packageNames 삭제할 앱 패키지명 목록 */
    suspend operator fun invoke(packageNames: List<String>) =
        repository.deleteNotificationsByPackages(packageNames)
}
