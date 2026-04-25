package com.android.notikeep.presentation.home

import com.android.notikeep.domain.model.AppNotification

data class HomeUiState(
    val notifications: List<AppNotification> = emptyList(),
    val isLoading: Boolean = false
)
