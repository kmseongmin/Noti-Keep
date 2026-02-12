package com.alarm.notikeep.presentation.notification_list

import com.alarm.notikeep.domain.model.NotificationItem

data class NotificationListUiState(
    val notifications: List<NotificationItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
