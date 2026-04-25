package com.android.notikeep.presentation.conversation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import androidx.paging.cachedIn
import com.android.notikeep.domain.usecase.DeleteNotificationsByIdsUseCase
import com.android.notikeep.domain.usecase.GetAllNotificationIdsByConversationUseCase
import com.android.notikeep.domain.usecase.GetNotificationsByConversationUseCase
import com.android.notikeep.domain.usecase.MarkConversationAsReadUseCase
import com.android.notikeep.presentation.navigation.ConversationRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getNotificationsByConversationUseCase: GetNotificationsByConversationUseCase,
    private val getAllNotificationIdsByConversationUseCase: GetAllNotificationIdsByConversationUseCase,
    private val deleteNotificationsByIdsUseCase: DeleteNotificationsByIdsUseCase,
    private val markConversationAsReadUseCase: MarkConversationAsReadUseCase
) : ViewModel() {

    private val route = savedStateHandle.toRoute<ConversationRoute>()
    private val packageName: String = route.packageName
    private val conversationKey: String = route.conversationKey

    private val _uiState = MutableStateFlow(ConversationUiState(isLoading = true))
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    val notifications = getNotificationsByConversationUseCase(packageName, conversationKey)
        .cachedIn(viewModelScope)

    init {
        viewModelScope.launch { markConversationAsReadUseCase(packageName, conversationKey) }
        _uiState.value = ConversationUiState(title = conversationKey, isLoading = false)
    }

    fun onItemLongClick(id: Long) {
        val selected = _uiState.value.selectedNotificationIds.toMutableSet()
        selected.add(id)
        _uiState.value = _uiState.value.copy(
            isSelectionMode = true,
            isAllSelected = false,
            selectedNotificationIds = selected
        )
    }

    fun onItemClick(id: Long) {
        if (!_uiState.value.isSelectionMode) return
        toggleNotificationSelection(id)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(
            isSelectionMode = false,
            isAllSelected = false,
            selectedNotificationIds = emptySet()
        )
    }

    fun selectAll() {
        viewModelScope.launch {
            val allIds = getAllNotificationIdsByConversationUseCase(packageName, conversationKey).toSet()
            val allSelected = allIds.isNotEmpty() &&
                _uiState.value.selectedNotificationIds.size == allIds.size &&
                _uiState.value.selectedNotificationIds.containsAll(allIds)
            val next = if (allSelected) emptySet() else allIds
            _uiState.value = _uiState.value.copy(
                isSelectionMode = true,
                isAllSelected = !allSelected && next.isNotEmpty(),
                selectedNotificationIds = next
            )
        }
    }

    fun deleteSelected() {
        val targets = _uiState.value.selectedNotificationIds.toList()
        if (targets.isEmpty()) return
        viewModelScope.launch {
            deleteNotificationsByIdsUseCase(targets)
            _uiState.value = _uiState.value.copy(
                isSelectionMode = true,
                isAllSelected = false,
                selectedNotificationIds = emptySet()
            )
        }
    }

    private fun toggleNotificationSelection(id: Long) {
        val selected = _uiState.value.selectedNotificationIds.toMutableSet()
        if (!selected.add(id)) selected.remove(id)
        _uiState.value = _uiState.value.copy(
            isAllSelected = false,
            selectedNotificationIds = selected
        )
    }
}
