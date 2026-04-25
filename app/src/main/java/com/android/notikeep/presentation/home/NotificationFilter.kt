package com.android.notikeep.presentation.home

import android.app.Notification

enum class NotificationFilter(val label: String) {
    ALL("전체"),
    MESSAGE("메시지");

    fun matches(category: String?): Boolean = when (this) {
        ALL -> true
        MESSAGE -> category == Notification.CATEGORY_MESSAGE
    }
}
