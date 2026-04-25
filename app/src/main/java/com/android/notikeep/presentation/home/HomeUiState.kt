package com.android.notikeep.presentation.home

import com.android.notikeep.domain.model.AppNotification

data class AppGroup(
    val packageName: String,
    val appName: String,
    val latest: AppNotification,
    val count: Int,
    val unreadCount: Int
)

data class HomeUiState(
    val appGroups: List<AppGroup> = emptyList(),
    val selectedFilter: NotificationFilter = NotificationFilter.ALL,
    val isLoading: Boolean = false
)
