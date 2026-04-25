package com.android.notikeep.domain.model

data class AppNotification(
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val title: String,
    val content: String,
    val receivedAt: Long,
    val category: String?
)
