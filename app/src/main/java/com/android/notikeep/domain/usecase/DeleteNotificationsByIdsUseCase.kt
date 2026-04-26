package com.android.notikeep.domain.usecase

import com.android.notikeep.domain.repository.NotificationRepository
import javax.inject.Inject

/**
 * Conversation 화면 선택 삭제용: 선택된 알림 ID들만 삭제하는 UseCase.
 * ConversationViewModel.deleteSelected() 에서 호출.
 */
class DeleteNotificationsByIdsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    /** @param ids 삭제할 알림 ID 목록 */
    suspend operator fun invoke(ids: List<Long>) =
        repository.deleteNotificationsByIds(ids)
}
