package com.android.notikeep.presentation.conversation

data class ConversationUiState(
    val title: String = "",
    val isSelectionMode: Boolean = false,
    val isAllSelected: Boolean = false,
    val selectedNotificationIds: Set<Long> = emptySet(),
    val isLoading: Boolean = false
)
