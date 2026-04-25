package com.android.notikeep.presentation.conversation

import com.android.notikeep.domain.model.AppNotification

data class ConversationUiState(
    val title: String = "",
    val notifications: List<AppNotification> = emptyList(),
    val isLoading: Boolean = false
)
