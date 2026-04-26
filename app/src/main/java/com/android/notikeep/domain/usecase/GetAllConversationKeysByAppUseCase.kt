package com.android.notikeep.domain.usecase

import com.android.notikeep.domain.repository.NotificationRepository
import javax.inject.Inject

/**
 * AppDetail 화면 "모두 선택" 기능용: 특정 앱의 모든 conversationKey 목록을 1회 조회하는 UseCase.
 * AppDetailViewModel.selectAll() 에서 호출 → 전체 키 목록과 현재 선택 목록을 비교해 토글.
 */
class GetAllConversationKeysByAppUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    /**
     * @param packageName 조회할 앱의 패키지명
     * @return conversationKey 목록 (중복 없음)
     */
    suspend operator fun invoke(packageName: String): List<String> =
        repository.getAllConversationKeysByApp(packageName)
}
