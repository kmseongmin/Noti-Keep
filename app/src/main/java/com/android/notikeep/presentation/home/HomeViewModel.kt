package com.android.notikeep.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.notikeep.domain.usecase.GetNotificationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        getNotificationsUseCase()
            .map { notifications ->
                val groups = notifications
                    .groupBy { it.packageName }
                    .map { (_, group) ->
                        AppGroup(
                            packageName = group.first().packageName,
                            appName = group.first().appName,
                            latest = group.first(), // receivedAt DESC 정렬이므로 첫 번째가 최신
                            count = group.size
                        )
                    }
                HomeUiState(appGroups = groups)
            }
            .onEach { newState -> _uiState.value = newState }
            .launchIn(viewModelScope)
    }
}
