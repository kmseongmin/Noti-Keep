package com.android.notikeep.domain.model

/**
 * AppDetail 화면에서 대화방 단위로 그룹핑된 알림 요약 도메인 모델.
 * ConversationGroupRow (data 레이어) → ConversationGroupSummary (domain 레이어) 변환 후 UseCase를 통해 ViewModel로 전달.
 */
data class ConversationGroupSummary(
    /**
     * 대화방 식별 키.
     * - 단체 채팅: subText (방 이름)
     * - 1:1 채팅 / 일반 앱: title (보낸 사람 이름)
     */
    val conversationKey: String,

    /** 앱 패키지명 */
    val packageName: String,

    /** 앱 이름 */
    val appName: String,

    /** 가장 최근 알림의 title (발신자 이름) */
    val latestTitle: String,

    /** 가장 최근 알림의 content (메시지 본문) */
    val latestContent: String,

    /** 가장 최근 알림의 subText (단체방 이름). 1:1이면 null */
    val latestSubText: String?,

    /** 가장 최근 알림의 카테고리 */
    val latestCategory: String?,

    /** 가장 최근 알림 수신 시각 (밀리초) */
    val latestReceivedAt: Long,

    /** 이 대화방에 저장된 알림 총 개수 */
    val count: Int,

    /** 이 대화방에서 안읽은 알림 수. 배지(Badge)에 표시 */
    val unreadCount: Int
)
