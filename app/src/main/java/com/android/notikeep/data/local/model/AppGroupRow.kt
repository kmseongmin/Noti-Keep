package com.android.notikeep.data.local.model

data class AppGroupRow(
    val packageName: String,
    val appName: String,
    val latestTitle: String,
    val latestContent: String,
    val latestCategory: String?,
    val latestReceivedAt: Long,
    val totalCount: Int,
    val unreadCount: Int
)
