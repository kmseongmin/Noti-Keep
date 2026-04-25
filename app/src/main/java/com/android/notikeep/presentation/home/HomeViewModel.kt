package com.android.notikeep.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.notikeep.domain.usecase.GetNotificationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(NotificationFilter.ALL)
    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        combine(getNotificationsUseCase(), _selectedFilter) { notifications, filter ->
            val groups = notifications
                .filter { filter.matches(it.category) }
                .groupBy { it.packageName }
                .map { (_, group) ->
                    AppGroup(
                        packageName = group.first().packageName,
                        appName = group.first().appName,
                        latest = group.first(),
                        count = group.size,
                        unreadCount = group.count { !it.isRead }
                    )
                }
            HomeUiState(appGroups = groups, selectedFilter = filter)
        }
            .onEach { newState -> _uiState.value = newState }
            .launchIn(viewModelScope)
    }

    fun setFilter(filter: NotificationFilter) {
        _selectedFilter.value = filter
    }
}
