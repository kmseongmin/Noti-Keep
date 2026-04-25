package com.android.notikeep.presentation.appdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.notikeep.domain.usecase.GetNotificationsByAppUseCase
import com.android.notikeep.domain.usecase.MarkAppAsReadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getNotificationsByAppUseCase: GetNotificationsByAppUseCase,
    private val markAppAsReadUseCase: MarkAppAsReadUseCase
) : ViewModel() {

    private val packageName: String = checkNotNull(savedStateHandle["packageName"])

    private val _uiState = MutableStateFlow(AppDetailUiState(isLoading = true))
    val uiState: StateFlow<AppDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { markAppAsReadUseCase(packageName) }

        getNotificationsByAppUseCase(packageName)
            .map { notifications ->
                val conversations = notifications
                    .groupBy { it.conversationKey }
                    .map { (key, group) ->
                        ConversationGroup(
                            conversationKey = key,
                            latest = group.first(),
                            count = group.size
                        )
                    }
                AppDetailUiState(
                    appName = notifications.firstOrNull()?.appName.orEmpty(),
                    conversations = conversations
                )
            }
            .onEach { newState -> _uiState.value = newState }
            .launchIn(viewModelScope)
    }
}
