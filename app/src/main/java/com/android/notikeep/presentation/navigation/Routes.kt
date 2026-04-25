package com.android.notikeep.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data class AppDetailRoute(
    val packageName: String
)

@Serializable
data class ConversationRoute(
    val packageName: String,
    val conversationKey: String
)
