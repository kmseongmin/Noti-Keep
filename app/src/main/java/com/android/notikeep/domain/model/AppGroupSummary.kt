package com.android.notikeep.domain.model

/**
 * 홈 화면에서 앱 단위로 그룹핑된 알림 요약 도메인 모델.
 * AppGroupRow (data 레이어) → AppGroupSummary (domain 레이어) 변환 후 UseCase를 통해 ViewModel로 전달.
 */
data class AppGroupSummary(
    /** 앱 패키지명. 화면 이동 시 key로 사용 */
    val packageName: String,

    /** 앱 이름. 홈 화면 목록에 표시 */
    val appName: String,

    /** 가장 최근 알림의 title (발신자 이름). 목록 미리보기에 표시 */
    val latestTitle: String,

    /** 가장 최근 알림의 content (메시지 본문). 목록 미리보기에 표시 */
    val latestContent: String,

    /** 가장 최근 알림의 카테고리. 메시지 필터링에 사용 */
    val latestCategory: String?,

    /** 가장 최근 알림 수신 시각 (밀리초). 목록 정렬 기준 */
    val latestReceivedAt: Long,

    /** 이 앱 전체 저장 알림 수 */
    val totalCount: Int,

    /** 이 앱에서 안읽은 알림 수. 배지(Badge)에 표시 */
    val unreadCount: Int
)
