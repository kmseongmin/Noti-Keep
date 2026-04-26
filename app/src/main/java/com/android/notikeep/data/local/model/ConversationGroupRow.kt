package com.android.notikeep.data.local.model

/**
 * DAO의 getConversationGroupsByApp() 쿼리 결과를 담는 data 클래스.
 * 특정 앱 내에서 대화방 단위로 그룹핑한 "1행 = 대화방 1개" 형태.
 * data 레이어에서만 사용하고, domain 레이어에는 ConversationGroupSummary로 변환해서 넘긴다.
 */
data class ConversationGroupRow(
    /**
     * 대화 그룹핑 키.
     * - subText(단체방 이름)가 있으면 subText
     * - 없으면 title(보낸 사람 이름)
     * SQL: CASE WHEN subText IS NOT NULL AND subText != '' THEN subText ELSE title END
     */
    val conversationKey: String,

    /** 앱 패키지명 */
    val packageName: String,

    /** 앱 이름 */
    val appName: String,

    /** 이 대화방의 가장 최근 알림 title (보낸 사람 이름) */
    val latestTitle: String,

    /** 이 대화방의 가장 최근 알림 content (메시지 본문) */
    val latestContent: String,

    /** 이 대화방의 가장 최근 알림 subText (단체방 이름). 1:1이면 null */
    val latestSubText: String?,

    /** 이 대화방의 가장 최근 알림 카테고리 */
    val latestCategory: String?,

    /** 이 대화방의 가장 최근 알림 수신 시각 (밀리초) */
    val latestReceivedAt: Long,

    /** 이 대화방에 저장된 알림 총 개수 */
    val count: Int,

    /** 이 대화방에서 아직 읽지 않은 알림 개수 (isRead = 0 인 것) */
    val unreadCount: Int
)
