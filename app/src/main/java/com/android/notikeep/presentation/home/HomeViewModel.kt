package com.android.notikeep.presentation.home

import android.app.Notification
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.android.notikeep.domain.usecase.DeleteNotificationsByPackagesUseCase
import com.android.notikeep.domain.usecase.GetAllPackageNamesUseCase
import com.android.notikeep.domain.usecase.GetNotificationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val getAllPackageNamesUseCase: GetAllPackageNamesUseCase,
    private val deleteNotificationsByPackagesUseCase: DeleteNotificationsByPackagesUseCase
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

    fun onItemLongClick(packageName: String) {
        val selected = _uiState.value.selectedPackageNames.toMutableSet()
        selected.add(packageName)
        _uiState.value = _uiState.value.copy(
            isSelectionMode = true,
            isAllSelected = false,
            selectedPackageNames = selected
        )
    }

    fun onItemClick(packageName: String) {
        if (!_uiState.value.isSelectionMode) return
        togglePackageSelection(packageName)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(
            isSelectionMode = false,
            isAllSelected = false,
            selectedPackageNames = emptySet()
        )
    }

    fun selectAll() {
        viewModelScope.launch {
            val allPackageNames = getAllPackageNamesUseCase(_selectedFilter.value.toCategory()).toSet()
            val allSelected = allPackageNames.isNotEmpty() &&
                _uiState.value.selectedPackageNames.size == allPackageNames.size &&
                _uiState.value.selectedPackageNames.containsAll(allPackageNames)
            val next = if (allSelected) emptySet() else allPackageNames
            _uiState.value = _uiState.value.copy(
                isSelectionMode = true,
                isAllSelected = !allSelected && next.isNotEmpty(),
                selectedPackageNames = next
            )
        }
    }

    fun deleteSelected() {
        val targets = _uiState.value.selectedPackageNames.toList()
        if (targets.isEmpty()) return
        viewModelScope.launch {
            deleteNotificationsByPackagesUseCase(targets)
            _uiState.value = _uiState.value.copy(
                isSelectionMode = true,
                isAllSelected = false,
                selectedPackageNames = emptySet()
            )
        }
    }

    private fun togglePackageSelection(packageName: String) {
        val selected = _uiState.value.selectedPackageNames.toMutableSet()
        if (!selected.add(packageName)) selected.remove(packageName)
        _uiState.value = _uiState.value.copy(
            isAllSelected = false,
            selectedPackageNames = selected
        )
    }
}

private fun NotificationFilter.toCategory(): String? = when (this) {
    NotificationFilter.ALL -> null
    NotificationFilter.MESSAGE -> Notification.CATEGORY_MESSAGE
}
