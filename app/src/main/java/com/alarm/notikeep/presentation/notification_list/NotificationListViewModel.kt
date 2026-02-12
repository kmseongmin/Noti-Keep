package com.alarm.notikeep.presentation.notification_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarm.notikeep.domain.model.NotificationItem
import com.alarm.notikeep.domain.usecase.GetAllNotificationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationListViewModel @Inject constructor(
    private val getAllNotificationsUseCase: GetAllNotificationsUseCase
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            getAllNotificationsUseCase().collect { notificationList ->
                _notifications.value = notificationList
            }
        }
    }
}
