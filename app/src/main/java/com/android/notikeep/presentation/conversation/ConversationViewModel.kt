package com.android.notikeep.presentation.conversation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.android.notikeep.domain.usecase.GetNotificationsByConversationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    val notifications = getNotificationsByConversationUseCase(packageName, conversationKey)
        .cachedIn(viewModelScope)

    init {
        _uiState.value = ConversationUiState(title = conversationKey, isLoading = false)
    }
}
