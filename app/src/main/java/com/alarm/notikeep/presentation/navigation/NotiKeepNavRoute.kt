package com.alarm.notikeep.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface NotiKeepNavRoute {
    @Serializable
    data object Permission : NotiKeepNavRoute

    @Serializable
    data object NotificationList : NotiKeepNavRoute
}
