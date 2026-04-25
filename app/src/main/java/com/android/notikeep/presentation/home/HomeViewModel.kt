package com.android.notikeep.presentation.home

import android.app.Notification
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.android.notikeep.domain.usecase.GetNotificationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(NotificationFilter.ALL)
    private val _uiState = MutableStateFlow(HomeUiState(selectedFilter = NotificationFilter.ALL))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val appGroups = _selectedFilter
        .flatMapLatest { filter ->
            getNotificationsUseCase(filter.toCategory()).map { pagingData ->
                pagingData.map { group ->
                    AppGroup(
                        packageName = group.packageName,
                        appName = group.appName,
                        latestTitle = group.latestTitle,
                        latestContent = group.latestContent,
                        latestCategory = group.latestCategory,
                        latestReceivedAt = group.latestReceivedAt,
                        count = group.totalCount,
                        unreadCount = group.unreadCount
                    )
                }
            }
        }
        .cachedIn(viewModelScope)

    fun setFilter(filter: NotificationFilter) {
        _selectedFilter.value = filter
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }
}

private fun NotificationFilter.toCategory(): String? = when (this) {
    NotificationFilter.ALL -> null
    NotificationFilter.MESSAGE -> Notification.CATEGORY_MESSAGE
}
