package com.android.notikeep.domain.usecase

import androidx.paging.PagingData
import com.android.notikeep.domain.model.ConversationGroupSummary
import com.android.notikeep.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * AppDetail 화면용: 특정 앱 내 대화방 단위 그룹핑 목록(페이징)을 반환하는 UseCase.
 * AppDetailViewModel 초기화 시 1회 호출되어 Flow를 구독함.
 */
class GetNotificationsByAppUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    /**
     * @param packageName 조회할 앱의 패키지명
     * @return 대화방 그룹 요약 목록의 페이징 Flow
     */
    operator fun invoke(packageName: String): Flow<PagingData<ConversationGroupSummary>> =
        repository.getConversationGroupsByApp(packageName)
}
