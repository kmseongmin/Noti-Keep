package com.android.notikeep.presentation.home

/**
 * 홈 화면의 앱 그룹 항목 UI 표현 모델.
 * AppGroupSummary (domain) → AppGroup (presentation) 변환은 HomeViewModel.appGroups에서 수행.
 */
data class AppGroup(
    /** 앱 패키지명. 앱 클릭 시 AppDetail로 이동할 때 전달 */
    val packageName: String,

    /** 앱 이름. 목록 항목 제목으로 표시 */
    val appName: String,

    /** 가장 최근 알림의 발신자 이름 (title). 미리보기 첫 번째 줄 */
    val latestTitle: String,

    /** 가장 최근 알림 본문 (content). 미리보기 두 번째 줄 */
    val latestContent: String,

    /** 가장 최근 알림 카테고리. 메시지 여부 판단에 사용 */
    val latestCategory: String?,

    /** 가장 최근 알림 수신 시각 (밀리초). 목록 오른쪽 시간 표시 */
    val latestReceivedAt: Long,

    /** 이 앱의 전체 저장 알림 수 */
    val count: Int,

    /** 이 앱에서 안읽은 알림 수. 0보다 크면 배지(Badge) 표시 */
    val unreadCount: Int
)

/**
 * 홈 화면 전체 UI 상태.
 * HomeViewModel에서 StateFlow<HomeUiState>로 관리.
 */
data class HomeUiState(
    /** 현재 선택된 필터 (전체 / 메시지). 기본값 ALL */
    val selectedFilter: NotificationFilter = NotificationFilter.ALL,

    /**
     * 선택 모드 활성화 여부.
     * - false: 일반 탐색 모드 (클릭 → AppDetail 이동)
     * - true: 선택 삭제 모드 (클릭 → 선택 토글, 상단 액션바 표시)
     */
    val isSelectionMode: Boolean = false,

    /**
     * 전체 선택 여부. "모두선택" 버튼 텍스트(모두선택↔전체해제) 전환에 사용.
     * selectedPackageNames.size == 전체 패키지 수 일 때 true
     */
    val isAllSelected: Boolean = false,

    /** 현재 선택된 패키지명 집합. 삭제 시 이 목록을 사용 */
    val selectedPackageNames: Set<String> = emptySet(),

    /** 데이터 로딩 중 여부 (현재 미사용, 페이징 LoadState로 대체) */
    val isLoading: Boolean = false
)
