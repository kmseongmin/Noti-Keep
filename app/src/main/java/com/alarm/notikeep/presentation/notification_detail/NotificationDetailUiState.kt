package com.alarm.notikeep.presentation.notification_detail

import com.alarm.notikeep.domain.model.NotificationItem

data class NotificationDetailUiState(
    val notifications: List<NotificationItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
