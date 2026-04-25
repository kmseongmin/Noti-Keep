package com.android.notikeep.presentation.appdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.android.notikeep.domain.model.AppNotification
import com.android.notikeep.domain.usecase.GetNotificationsByAppUseCase
import com.android.notikeep.domain.usecase.GetLatestAppNameUseCase
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
    private val getLatestAppNameUseCase: GetLatestAppNameUseCase,
    private val markAppAsReadUseCase: MarkAppAsReadUseCase
) : ViewModel() {

    private val packageName: String = checkNotNull(savedStateHandle["packageName"])

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
                    count = group.count
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
}
