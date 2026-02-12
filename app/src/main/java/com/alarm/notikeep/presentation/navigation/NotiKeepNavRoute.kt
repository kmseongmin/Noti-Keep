package com.alarm.notikeep.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface NotiKeepNavRoute {
    @Serializable
    data object Permission : NotiKeepNavRoute

    @Serializable
    data object NotificationList : NotiKeepNavRoute

    @Serializable
    data class NotificationGroupDetail(
        val packageName: String,
        val threadKey: String,
        val title: String? = null
    ) : NotiKeepNavRoute
}
