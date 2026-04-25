package com.android.notikeep.presentation.appdetail

import com.android.notikeep.domain.model.AppNotification

data class ConversationGroup(
    val title: String,
    val latest: AppNotification,
    val count: Int
)

data class AppDetailUiState(
    val appName: String = "",
    val conversations: List<ConversationGroup> = emptyList(),
    val isLoading: Boolean = false
)
