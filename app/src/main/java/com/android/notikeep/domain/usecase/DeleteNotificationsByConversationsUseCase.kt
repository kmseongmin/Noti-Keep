package com.android.notikeep.domain.usecase

import com.android.notikeep.domain.repository.NotificationRepository
import javax.inject.Inject

/**
 * AppDetail 화면 선택 삭제용: 선택된 대화방들의 알림을 전부 삭제하는 UseCase.
 * AppDetailViewModel.deleteSelected() 에서 호출.
 */
class DeleteNotificationsByConversationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    /**
     * @param packageName 앱 패키지명
     * @param conversationKeys 삭제할 대화방 키 목록
     */
    suspend operator fun invoke(packageName: String, conversationKeys: List<String>) =
        repository.deleteNotificationsByConversations(packageName, conversationKeys)
}
