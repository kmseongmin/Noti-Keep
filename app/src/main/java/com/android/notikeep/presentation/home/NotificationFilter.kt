package com.android.notikeep.presentation.home

import android.app.Notification

/**
 * 홈 화면 알림 필터 종류.
 * FilterChip UI에서 선택 → HomeViewModel._selectedFilter 업데이트 → DB 쿼리 재실행.
 *
 * @param label FilterChip에 표시되는 한국어 라벨
 */
enum class NotificationFilter(val label: String) {
    /** 카테고리 무관 전체 알림 */
    ALL("전체"),

    /** category = Notification.CATEGORY_MESSAGE("msg") 인 알림만 */
    MESSAGE("메시지");

    /**
     * 주어진 category 값이 이 필터에 해당하는지 판단.
     * UI 레이어에서 직접 사용하지는 않고 toCategory() 확장함수를 통해 DB 쿼리 파라미터로 변환됨.
     */
    fun matches(category: String?): Boolean = when (this) {
        ALL -> true
        MESSAGE -> category == Notification.CATEGORY_MESSAGE
    }
}
