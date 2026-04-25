package com.android.notikeep.presentation.home

data class AppGroup(
    val packageName: String,
    val appName: String,
    val latestTitle: String,
    val latestContent: String,
    val latestCategory: String?,
    val latestReceivedAt: Long,
    val count: Int,
    val unreadCount: Int
)

data class HomeUiState(
    val selectedFilter: NotificationFilter = NotificationFilter.ALL,
    val isSelectionMode: Boolean = false,
    val isAllSelected: Boolean = false,
    val selectedPackageNames: Set<String> = emptySet(),
    val isLoading: Boolean = false
)
