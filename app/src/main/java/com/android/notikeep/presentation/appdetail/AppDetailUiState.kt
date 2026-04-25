package com.android.notikeep.presentation.appdetail

import com.android.notikeep.domain.model.AppNotification

data class ConversationGroup(
    val conversationKey: String, // 단톡방이면 방 이름, 개인이면 보낸 사람
    val latest: AppNotification,
    val count: Int,
    val unreadCount: Int
)

data class AppDetailUiState(
    val appName: String = "",
    val isSelectionMode: Boolean = false,
    val isAllSelected: Boolean = false,
    val selectedConversationKeys: Set<String> = emptySet(),
    val isLoading: Boolean = false
)
