package com.android.notikeep.presentation.appdetail

import com.android.notikeep.domain.model.AppNotification

/**
 * AppDetail 화면의 대화방 그룹 항목 UI 표현 모델.
 * ConversationGroupSummary (domain) → ConversationGroup (presentation) 변환은 AppDetailViewModel.conversations에서 수행.
 */
data class ConversationGroup(
    /**
     * 대화방 식별 키.
     * - 단체 채팅방: subText (방 이름)
     * - 1:1 채팅 / 일반 앱: title (발신자 이름)
     * Conversation 화면으로 이동 시 전달됨.
     */
    val conversationKey: String,

    /**
     * 이 대화방의 가장 최근 알림 정보.
     * 목록 미리보기(최신 메시지, 시간) 및 카테고리 판단에 사용
     */
    val latest: AppNotification,

    /** 이 대화방 전체 저장 알림 수 */
    val count: Int,

    /** 이 대화방에서 안읽은 알림 수. 0보다 크면 배지(Badge) 표시 */
    val unreadCount: Int
)

/**
 * AppDetail 화면 전체 UI 상태.
 * AppDetailViewModel에서 StateFlow<AppDetailUiState>로 관리.
 */
data class AppDetailUiState(
    /** 상단 TopAppBar 타이틀. GetLatestAppNameUseCase에서 실시간 업데이트 */
    val appName: String = "",

    /**
     * 선택 모드 활성화 여부.
     * - false: 일반 탐색 모드 (메시지 항목 클릭 → Conversation 이동)
     * - true: 선택 삭제 모드
     */
    val isSelectionMode: Boolean = false,

    /**
     * 전체 선택 여부.
     * selectedConversationKeys.size == 전체 대화방 수 일 때 true
     */
    val isAllSelected: Boolean = false,

    /** 현재 선택된 conversationKey 집합. 삭제 시 이 목록을 사용 */
    val selectedConversationKeys: Set<String> = emptySet(),

    /** 초기 로딩 중 여부. init에서 appName을 받아오기 전까지 true */
    val isLoading: Boolean = false
)
