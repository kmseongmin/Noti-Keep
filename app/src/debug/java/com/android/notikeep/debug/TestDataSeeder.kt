package com.android.notikeep.debug

import android.app.Notification
import com.android.notikeep.domain.model.AppNotification
import com.android.notikeep.domain.usecase.SaveNotificationUseCase
import javax.inject.Inject

class TestDataSeeder @Inject constructor(
    private val saveNotificationUseCase: SaveNotificationUseCase
) {
    suspend fun seed() {
        val now = System.currentTimeMillis()

        testData(now).forEach { saveNotificationUseCase(it) }
    }

    private fun testData(now: Long) = listOf(
        // 카카오톡 - 개인 DM
        AppNotification(packageName = "com.kakao.talk", appName = "카카오톡", title = "김철수", content = "야 밥 먹었어?", subText = null, category = Notification.CATEGORY_MESSAGE, receivedAt = now - 60_000),
        AppNotification(packageName = "com.kakao.talk", appName = "카카오톡", title = "김철수", content = "ㅋㅋㅋㅋ", subText = null, category = Notification.CATEGORY_MESSAGE, receivedAt = now - 30_000),

        // 카카오톡 - 단톡방
        AppNotification(packageName = "com.kakao.talk", appName = "카카오톡", title = "이영희", content = "회의 몇 시야?", subText = "프로젝트팀", category = Notification.CATEGORY_MESSAGE, receivedAt = now - 120_000),
        AppNotification(packageName = "com.kakao.talk", appName = "카카오톡", title = "박민수", content = "3시로 하자", subText = "프로젝트팀", category = Notification.CATEGORY_MESSAGE, receivedAt = now - 90_000),
        AppNotification(packageName = "com.kakao.talk", appName = "카카오톡", title = "이영희", content = "ㅇㅋ", subText = "프로젝트팀", category = Notification.CATEGORY_MESSAGE, receivedAt = now - 50_000),

        // 쿠팡 - 일반 알림
        AppNotification(packageName = "com.coupang.mobile", appName = "쿠팡", title = "오늘의 특가!", content = "로켓배송 상품 최대 50% 할인", subText = null, category = null, receivedAt = now - 200_000),
        AppNotification(packageName = "com.coupang.mobile", appName = "쿠팡", title = "배송 완료", content = "주문하신 상품이 배송 완료되었습니다.", subText = null, category = null, receivedAt = now - 100_000),

        // 구글 날씨
        AppNotification(packageName = "com.google.android.googlequicksearchbox", appName = "Google", title = "부천시", content = "앞으로 3일 동안 기온이 낮아질 것으로 예상됩니다. 부천시의 전체 일기예보 확인", subText = null, category = null, receivedAt = now - 300_000),

        // Gmail
        AppNotification(packageName = "com.google.android.gm", appName = "Gmail", title = "GitHub", content = "Your pull request has been merged", subText = null, category = Notification.CATEGORY_EMAIL, receivedAt = now - 400_000),
    )
}
