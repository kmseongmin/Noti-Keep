package com.android.notikeep.presentation.appdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import androidx.paging.cachedIn
import androidx.paging.map
import com.android.notikeep.domain.model.AppNotification
import com.android.notikeep.domain.usecase.DeleteNotificationsByConversationsUseCase
import com.android.notikeep.domain.usecase.GetAllConversationKeysByAppUseCase
import com.android.notikeep.domain.usecase.GetNotificationsByAppUseCase
import com.android.notikeep.domain.usecase.GetLatestAppNameUseCase
import com.android.notikeep.domain.usecase.MarkAppAsReadUseCase
import com.android.notikeep.presentation.navigation.AppDetailRoute
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
    private val getLatestAppNameUseCase: GetLatestAppNameUseCase,
    private val markAppAsReadUseCase: MarkAppAsReadUseCase,
    private val getAllConversationKeysByAppUseCase: GetAllConversationKeysByAppUseCase,
    private val deleteNotificationsByConversationsUseCase: DeleteNotificationsByConversationsUseCase
) : ViewModel() {

    private val packageName: String = savedStateHandle.toRoute<AppDetailRoute>().packageName

    private val _uiState = MutableStateFlow(AppDetailUiState(isLoading = true))
    val uiState: StateFlow<AppDetailUiState> = _uiState.asStateFlow()

    val conversations = getNotificationsByAppUseCase(packageName)
        .map { pagingData ->
            pagingData.map { group ->
                ConversationGroup(
                    conversationKey = group.conversationKey,
                    latest = AppNotification(
                        packageName = group.packageName,
                        appName = group.appName,
                        title = group.latestTitle,
                        content = group.latestContent,
                        subText = group.latestSubText,
                        category = group.latestCategory,
                        receivedAt = group.latestReceivedAt
                    ),
                    count = group.count,
                    unreadCount = group.unreadCount
                )
            }
        }
        .cachedIn(viewModelScope)

    init {
        viewModelScope.launch { markAppAsReadUseCase(packageName) }

        getLatestAppNameUseCase(packageName)
            .onEach { appName ->
                _uiState.value = _uiState.value.copy(
                    appName = appName.orEmpty(),
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }

    fun onItemLongClick(conversationKey: String) {
        val selected = _uiState.value.selectedConversationKeys.toMutableSet()
        selected.add(conversationKey)
        _uiState.value = _uiState.value.copy(
            isSelectionMode = true,
            isAllSelected = false,
            selectedConversationKeys = selected
        )
    }

    fun onItemClick(conversationKey: String) {
        if (!_uiState.value.isSelectionMode) return
        toggleConversationSelection(conversationKey)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(
            isSelectionMode = false,
            isAllSelected = false,
            selectedConversationKeys = emptySet()
        )
    }

    fun selectAll() {
        viewModelScope.launch {
            val allKeys = getAllConversationKeysByAppUseCase(packageName).toSet()
            val allSelected = allKeys.isNotEmpty() &&
                _uiState.value.selectedConversationKeys.size == allKeys.size &&
                _uiState.value.selectedConversationKeys.containsAll(allKeys)
            val next = if (allSelected) emptySet() else allKeys
            _uiState.value = _uiState.value.copy(
                isSelectionMode = true,
                isAllSelected = !allSelected && next.isNotEmpty(),
                selectedConversationKeys = next
            )
        }
    }

    fun deleteSelected() {
        val targets = _uiState.value.selectedConversationKeys.toList()
        if (targets.isEmpty()) return
        viewModelScope.launch {
            deleteNotificationsByConversationsUseCase(packageName, targets)
            _uiState.value = _uiState.value.copy(
                isSelectionMode = true,
                isAllSelected = false,
                selectedConversationKeys = emptySet()
            )
        }
    }

    private fun toggleConversationSelection(conversationKey: String) {
        val selected = _uiState.value.selectedConversationKeys.toMutableSet()
        if (!selected.add(conversationKey)) selected.remove(conversationKey)
        _uiState.value = _uiState.value.copy(
            isAllSelected = false,
            selectedConversationKeys = selected
        )
    }
}
