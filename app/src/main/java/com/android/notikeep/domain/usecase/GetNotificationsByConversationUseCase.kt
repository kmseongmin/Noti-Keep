package com.android.notikeep.domain.usecase

import androidx.paging.PagingData
import com.android.notikeep.domain.model.AppNotification
import com.android.notikeep.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Conversation 화면용: 특정 대화방의 알림 목록(페이징)을 반환하는 UseCase.
 * ConversationViewModel 초기화 시 1회 호출되어 Flow를 구독함.
 */
class GetNotificationsByConversationUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    /**
     * @param packageName 앱 패키지명
     * @param conversationKey 대화방 키 (단체방 이름 또는 발신자 이름)
     * @return 알림 목록의 페이징 Flow (최신순)
     */
    operator fun invoke(
        packageName: String,
        conversationKey: String
    ): Flow<PagingData<AppNotification>> =
        repository.getNotificationsByConversation(packageName, conversationKey)
}
