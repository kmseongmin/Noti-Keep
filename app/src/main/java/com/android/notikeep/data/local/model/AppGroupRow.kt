package com.android.notikeep.data.local.model

/**
 * DAO의 getAppGroups() 쿼리 결과를 담는 data 클래스.
 * 앱 단위로 알림을 그룹핑한 "1행 = 앱 1개" 형태.
 * data 레이어에서만 사용하고, domain 레이어에는 AppGroupSummary로 변환해서 넘긴다.
 */
data class AppGroupRow(
    /** 앱 패키지명. 그룹핑 기준 키 */
    val packageName: String,

    /** 앱 이름 (가장 최근 알림 기준) */
    val appName: String,

    /** 가장 최근 알림의 title (보낸 사람 이름) */
    val latestTitle: String,

    /** 가장 최근 알림의 content (메시지 본문) */
    val latestContent: String,

    /** 가장 최근 알림의 카테고리 */
    val latestCategory: String?,

    /** 가장 최근 알림의 수신 시각 (밀리초) */
    val latestReceivedAt: Long,

    /** 이 앱에 저장된 알림 총 개수 */
    val totalCount: Int,

    /** 이 앱에서 아직 읽지 않은 알림 개수 (isRead = 0 인 것) */
    val unreadCount: Int
)
