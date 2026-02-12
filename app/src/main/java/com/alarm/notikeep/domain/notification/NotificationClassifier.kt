package com.alarm.notikeep.domain.notification

import com.alarm.notikeep.domain.model.NotificationItem
import java.util.Locale

object NotificationClassifier {
    // 메인 목록 그룹핑 키:
    // - 메시지 알림은 conversationKey(채팅방/상대) 기준
    // - 일반 알림은 appName 기준
    fun threadKey(item: NotificationItem): String {
        if (isMessageNotification(item)) {
            val conversationToken = normalizeText(item.conversationKey)
            if (conversationToken.isNotBlank()) {
                return "${normalizeText(item.packageName)}::msg::$conversationToken"
            }
            val titleToken = normalizeText(item.title)
            if (titleToken.isNotBlank()) {
                return "${normalizeText(item.packageName)}::msg::$titleToken"
            }
        }

        val appNameToken = normalizeText(item.appName)
        return if (appNameToken.isNotBlank()) {
            appNameToken
        } else {
            normalizeText(item.packageName)
        }
    }

    // 저장 중복 판정 키: 같은 그룹 내 동일 내용 알림 재저장을 방지한다.
    fun dedupKey(item: NotificationItem): String {
        val contentToken = normalizeText(item.content)
        return "${threadKey(item)}::$contentToken"
    }

    fun isMessageNotification(item: NotificationItem): Boolean {
        return normalizeText(item.category) == "msg"
    }

    // 숫자/퍼센트/시간처럼 자주 바뀌는 토큰을 일반화해서 안정적인 키를 만든다.
    fun normalizeText(text: String?): String {
        if (text.isNullOrBlank()) return ""

        return text
            .lowercase(Locale.getDefault())
            .replace(Regex("\\d+%"), "{percent}")
            .replace(Regex("\\d{1,2}:\\d{2}"), "{time}")
            .replace(Regex("\\d+"), "{num}")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
