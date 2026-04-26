package com.android.notikeep.domain.usecase

import androidx.paging.PagingData
import com.android.notikeep.domain.model.AppGroupSummary
import com.android.notikeep.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 홈 화면용: 앱 단위로 그룹핑된 알림 목록(페이징)을 반환하는 UseCase.
 * HomeViewModel에서 필터 변경 시마다 flatMapLatest로 재호출됨.
 */
class GetNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    /**
     * @param category null = 전체, "msg" = 메시지 카테고리만
     * @return 앱 그룹 요약 목록의 페이징 Flow
     */
    operator fun invoke(category: String?): Flow<PagingData<AppGroupSummary>> =
        repository.getAppGroups(category)
}
