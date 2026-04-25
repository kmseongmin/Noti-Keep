package com.android.notikeep.presentation.conversation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.notikeep.domain.usecase.GetNotificationsByConversationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ConversationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getNotificationsByConversationUseCase: GetNotificationsByConversationUseCase
) : ViewModel() {

    private val packageName: String = checkNotNull(savedStateHandle["packageName"])
    private val conversationKey: String = checkNotNull(savedStateHandle["conversationKey"])

    private val _uiState = MutableStateFlow(ConversationUiState(isLoading = true))
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    init {
        getNotificationsByConversationUseCase(packageName, conversationKey)
            .map { ConversationUiState(title = conversationKey, notifications = it) }
            .onEach { newState -> _uiState.value = newState }
            .launchIn(viewModelScope)
    }
}
