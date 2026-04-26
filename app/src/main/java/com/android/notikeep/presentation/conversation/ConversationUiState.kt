package com.android.notikeep.presentation.conversation

/**
 * Conversation 화면 전체 UI 상태.
 * ConversationViewModel에서 StateFlow<ConversationUiState>로 관리.
 */
data class ConversationUiState(
    /** 상단 TopAppBar 타이틀. conversationKey(대화방 이름 또는 발신자 이름)로 설정 */
    val title: String = "",

    /**
     * 선택 모드 활성화 여부.
     * - false: 일반 읽기 모드
     * - true: 선택 삭제 모드 (롱클릭으로 진입)
     */
    val isSelectionMode: Boolean = false,

    /**
     * 전체 선택 여부.
     * selectedNotificationIds.size == 대화방 전체 알림 수 일 때 true
     */
    val isAllSelected: Boolean = false,

    /** 현재 선택된 알림 ID 집합. 삭제 시 이 목록을 사용 */
    val selectedNotificationIds: Set<Long> = emptySet(),

    /** 초기 로딩 중 여부 (현재 init에서 즉시 false로 전환) */
    val isLoading: Boolean = false
)
