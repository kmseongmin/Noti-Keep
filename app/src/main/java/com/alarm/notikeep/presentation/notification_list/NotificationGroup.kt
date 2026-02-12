package com.alarm.notikeep.presentation.notification_list

import com.alarm.notikeep.domain.model.NotificationItem
import com.alarm.notikeep.domain.notification.NotificationClassifier

data class NotificationGroup(
    val threadKey: String,
    val packageName: String,
    val appName: String,
    val title: String?,
    val content: String?,
    val latestTimestamp: Long,
    val iconData: ByteArray?,
    val totalCount: Int,
    val hasUnread: Boolean
)

fun List<NotificationItem>.toNotificationGroups(): List<NotificationGroup> {
    // 저장된 개별 알림 이력을 threadKey 기준으로 묶어 메인 목록 1건으로 표시한다.
    return groupBy { NotificationClassifier.threadKey(it) }
        .map { (_, grouped) ->
            val latest = grouped.maxByOrNull { it.timestamp } ?: return@map null
            NotificationGroup(
                threadKey = NotificationClassifier.threadKey(latest),
                packageName = latest.packageName,
                appName = latest.appName,
                title = latest.title,
                content = latest.content,
                latestTimestamp = latest.timestamp,
                iconData = latest.iconData,
                totalCount = grouped.size,
                hasUnread = grouped.any { !it.isRead }
            )
        }
        .filterNotNull()
        .sortedByDescending { it.latestTimestamp }
}
