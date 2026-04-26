package com.android.notikeep.domain.usecase

import com.android.notikeep.domain.repository.NotificationRepository
import javax.inject.Inject

/**
 * 홈 화면 "모두 선택" 기능용: 현재 DB에 저장된 모든 앱 패키지명 목록을 1회 조회하는 UseCase.
 * HomeViewModel.selectAll() 에서 호출 → 전체 패키지 목록과 현재 선택 목록을 비교해 토글.
 */
class GetAllPackageNamesUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    /**
     * @param category null = 전체, 값 있으면 해당 카테고리만
     * @return 패키지명 목록 (중복 없음)
     */
    suspend operator fun invoke(category: String?): List<String> =
        repository.getAllPackageNames(category)
}
