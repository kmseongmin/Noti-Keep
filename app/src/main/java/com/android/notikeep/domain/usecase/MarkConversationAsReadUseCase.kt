package com.android.notikeep.domain.usecase

import com.android.notikeep.domain.repository.NotificationRepository
import javax.inject.Inject

/**
 * 특정 대화방의 모든 알림을 읽음(isRead=1) 처리하는 UseCase.
 * ConversationViewModel.init 블록에서 호출 → 대화 화면 진입 시 자동 읽음 처리.
 * 읽음 처리 후 Room이 DB 변경을 감지해 홈/AppDetail의 unreadCount가 자동 갱신됨.
 */
class MarkConversationAsReadUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    /**
     * @param packageName 앱 패키지명
     * @param conversationKey 읽음 처리할 대화방 키
     */
    suspend operator fun invoke(packageName: String, conversationKey: String) =
        repository.markConversationAsRead(packageName, conversationKey)
}
