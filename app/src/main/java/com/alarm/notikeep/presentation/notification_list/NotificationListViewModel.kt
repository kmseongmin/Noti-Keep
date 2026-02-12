package com.alarm.notikeep.presentation.notification_list

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarm.notikeep.domain.usecase.GetAllNotificationsUseCase
import com.alarm.notikeep.util.NotificationPermissionUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationListViewModel @Inject constructor(
    private val getAllNotificationsUseCase: GetAllNotificationsUseCase,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationListUiState())
    val uiState: StateFlow<NotificationListUiState> = _uiState.asStateFlow()

    init {
        checkPermission()
        loadNotifications()
    }

    fun checkPermission() {
        val hasPermission = NotificationPermissionUtil.isNotificationListenerEnabled(application)
        _uiState.update { it.copy(hasNotificationPermission = hasPermission) }
    }

    fun requestPermission() {
        NotificationPermissionUtil.openNotificationListenerSettings(application)
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getAllNotificationsUseCase().collect { notificationList ->
                _uiState.update {
                    it.copy(
                        notifications = notificationList,
                        isLoading = false
                    )
                }
            }
        }
    }
}
