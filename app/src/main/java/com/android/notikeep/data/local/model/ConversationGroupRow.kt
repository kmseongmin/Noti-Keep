package com.android.notikeep.data.local.model

data class ConversationGroupRow(
    val conversationKey: String,
    val packageName: String,
    val appName: String,
    val latestTitle: String,
    val latestContent: String,
    val latestSubText: String?,
    val latestCategory: String?,
    val latestReceivedAt: Long,
    val count: Int
)
