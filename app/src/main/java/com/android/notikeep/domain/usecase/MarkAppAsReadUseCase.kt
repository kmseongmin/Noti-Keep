package com.android.notikeep.domain.usecase

import com.android.notikeep.domain.repository.NotificationRepository
import javax.inject.Inject

/**
 * 특정 앱의 모든 알림을 읽음(isRead=1) 처리하는 UseCase.
 * 현재 어떤 ViewModel에서도 호출되지 않음 (미사용 상태).
 * 향후 AppDetail 화면 진입 시 앱 전체 읽음 처리에 활용 가능.
 */
class MarkAppAsReadUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    /** @param packageName 읽음 처리할 앱의 패키지명 */
    suspend operator fun invoke(packageName: String) = repository.markAppAsRead(packageName)
}
