package com.android.notikeep.presentation.appdetail

import android.util.Log
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

/**
 * AppDetail 화면 ViewModel.
 * 특정 앱의 대화방 목록 표시, 선택 모드 관리, 선택 삭제를 담당.
 */
@HiltViewModel
class AppDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getNotificationsByAppUseCase: GetNotificationsByAppUseCase,
    private val getLatestAppNameUseCase: GetLatestAppNameUseCase,
    private val getAllConversationKeysByAppUseCase: GetAllConversationKeysByAppUseCase,
    private val deleteNotificationsByConversationsUseCase: DeleteNotificationsByConversationsUseCase
) : ViewModel() {

    /**
     * 네비게이션 인자에서 추출한 앱 패키지명.
     * SavedStateHandle.toRoute<AppDetailRoute>()로 역직렬화.
     */
    private val packageName: String = savedStateHandle.toRoute<AppDetailRoute>().packageName

    /** 화면 전반의 UI 상태 (appName, 선택 모드, 선택된 항목 등) */
    private val _uiState = MutableStateFlow(AppDetailUiState(isLoading = true))
    val uiState: StateFlow<AppDetailUiState> = _uiState.asStateFlow()

    /**
     * 이 앱의 대화방 그룹 목록 (페이징).
     *
     * 동작 흐름:
     * 1. getNotificationsByAppUseCase(packageName)으로 DB 쿼리 Flow 구독
     * 2. PagingData<ConversationGroupSummary> → PagingData<ConversationGroup>으로 매핑
     *    - latest: AppNotification을 직접 생성 (isRead 등은 표시에 불필요해 기본값 사용)
     * 3. cachedIn으로 캐시
     */
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
        Log.d(TAG, "init - packageName=$packageName")
        // 앱 이름을 실시간으로 받아와 타이틀 업데이트
        getLatestAppNameUseCase(packageName)
            .onEach { appName ->
                Log.d(TAG, "appName 업데이트: $appName")
                _uiState.value = _uiState.value.copy(
                    appName = appName.orEmpty(),
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }

    /**
     * 항목 롱클릭 → 선택 모드 진입 + 해당 conversationKey 선택.
     */
    fun onItemLongClick(conversationKey: String) {
        Log.d(TAG, "onItemLongClick() - conversationKey=$conversationKey")
        val selected = _uiState.value.selectedConversationKeys.toMutableSet()
        selected.add(conversationKey)
        _uiState.value = _uiState.value.copy(
            isSelectionMode = true,
            isAllSelected = false,
            selectedConversationKeys = selected
        )
    }

    /**
     * 항목 클릭.
     * - 선택 모드가 아닌 경우: AppDetailScreen에서 onConversationClick 콜백 직접 처리 (Conversation 이동)
     * - 선택 모드인 경우: conversationKey 선택 토글
     */
    fun onItemClick(conversationKey: String) {
        if (!_uiState.value.isSelectionMode) return
        Log.d(TAG, "onItemClick() (선택 모드) - conversationKey=$conversationKey")
        toggleConversationSelection(conversationKey)
    }

    /**
     * 선택 모드 종료 + 선택 항목 초기화.
     * 뒤로가기(BackHandler) 또는 "해제" 버튼에서 호출.
     */
    fun clearSelection() {
        Log.d(TAG, "clearSelection() 호출")
        _uiState.value = _uiState.value.copy(
            isSelectionMode = false,
            isAllSelected = false,
            selectedConversationKeys = emptySet()
        )
    }

    /**
     * 전체 선택 / 전체 해제 토글.
     * 1. DB에서 현재 대화방 전체 키 목록 조회 (suspend)
     * 2. 현재 선택 목록과 비교 → 이미 전체 선택이면 해제, 아니면 전체 선택
     */
    fun selectAll() {
        viewModelScope.launch {
            val allKeys = getAllConversationKeysByAppUseCase(packageName).toSet()
            val allSelected = allKeys.isNotEmpty() &&
                _uiState.value.selectedConversationKeys.size == allKeys.size &&
                _uiState.value.selectedConversationKeys.containsAll(allKeys)
            val next = if (allSelected) emptySet() else allKeys
            Log.d(TAG, "selectAll() - 전체=${allKeys.size}개, 현재선택=${_uiState.value.selectedConversationKeys.size}개, allSelected=$allSelected")
            _uiState.value = _uiState.value.copy(
                isSelectionMode = true,
                isAllSelected = !allSelected && next.isNotEmpty(),
                selectedConversationKeys = next
            )
        }
    }

    /**
     * 선택된 대화방들의 알림 전체 삭제.
     * 삭제 완료 후 선택 모드 종료.
     */
    fun deleteSelected() {
        val targets = _uiState.value.selectedConversationKeys.toList()
        if (targets.isEmpty()) return
        Log.d(TAG, "deleteSelected() - 대상: $targets")
        viewModelScope.launch {
            deleteNotificationsByConversationsUseCase(packageName, targets)
            Log.d(TAG, "deleteSelected() 완료")
            _uiState.value = _uiState.value.copy(
                isSelectionMode = false,
                isAllSelected = false,
                selectedConversationKeys = emptySet()
            )
        }
    }

    /**
     * conversationKey 선택 토글.
     * - 이미 선택된 경우 → 제거
     * - 선택되지 않은 경우 → 추가
     */
    private fun toggleConversationSelection(conversationKey: String) {
        val selected = _uiState.value.selectedConversationKeys.toMutableSet()
        if (!selected.add(conversationKey)) selected.remove(conversationKey)
        Log.d(TAG, "toggleConversationSelection() - $conversationKey, 현재선택: $selected")
        _uiState.value = _uiState.value.copy(
            isAllSelected = false,
            selectedConversationKeys = selected
        )
    }

    companion object {
        private const val TAG = "AppDetailViewModel"
    }
}
