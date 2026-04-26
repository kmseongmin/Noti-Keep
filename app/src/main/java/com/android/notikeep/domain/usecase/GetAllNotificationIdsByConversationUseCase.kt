package com.android.notikeep.domain.usecase

import com.android.notikeep.domain.repository.NotificationRepository
import javax.inject.Inject

/**
 * Conversation 화면 "모두 선택" 기능용: 특정 대화방의 모든 알림 ID 목록을 1회 조회하는 UseCase.
 * ConversationViewModel.selectAll() 에서 호출 → 전체 ID 목록과 현재 선택 목록을 비교해 토글.
 */
class GetAllNotificationIdsByConversationUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    /**
     * @param packageName 앱 패키지명
     * @param conversationKey 대화방 키
     * @return 알림 ID 목록
     */
    suspend operator fun invoke(packageName: String, conversationKey: String): List<Long> =
        repository.getAllNotificationIdsByConversation(packageName, conversationKey)
}
