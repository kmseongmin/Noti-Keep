package com.android.notikeep.domain.usecase

import com.android.notikeep.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * AppDetail 화면 타이틀 표시용: 특정 앱의 가장 최신 앱 이름을 실시간으로 반환하는 UseCase.
 * AppDetailViewModel.init 에서 launchIn으로 구독 → uiState.appName 업데이트.
 */
class GetLatestAppNameUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    /**
     * @param packageName 조회할 앱의 패키지명
     * @return 앱 이름 Flow. 알림이 없으면 null emit
     */
    operator fun invoke(packageName: String): Flow<String?> =
        repository.getLatestAppName(packageName)
}
