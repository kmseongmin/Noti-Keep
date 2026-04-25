package com.android.notikeep.domain.model

data class ConversationGroupSummary(
    val conversationKey: String,
    val packageName: String,
    val appName: String,
    val latestTitle: String,
    val latestContent: String,
    val latestSubText: String?,
    val latestCategory: String?,
    val latestReceivedAt: Long,
    val count: Int,
    val unreadCount: Int
)
