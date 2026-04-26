package com.android.notikeep.presentation.conversation

import android.util.Log
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

/**
 * Conversation 화면 ViewModel.
 * 특정 대화방의 알림 목록 표시, 진입 시 자동 읽음 처리, 선택 삭제를 담당.
 */
@HiltViewModel
class ConversationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getNotificationsByConversationUseCase: GetNotificationsByConversationUseCase,
    private val getAllNotificationIdsByConversationUseCase: GetAllNotificationIdsByConversationUseCase,
    private val deleteNotificationsByIdsUseCase: DeleteNotificationsByIdsUseCase,
    private val markConversationAsReadUseCase: MarkConversationAsReadUseCase
) : ViewModel() {

    /**
     * 네비게이션 인자에서 추출한 앱 패키지명과 대화방 키.
     * SavedStateHandle.toRoute<ConversationRoute>()로 역직렬화.
     */
    private val route = savedStateHandle.toRoute<ConversationRoute>()
    private val packageName: String = route.packageName
    private val conversationKey: String = route.conversationKey

    /** 화면 전반의 UI 상태 (타이틀, 선택 모드, 선택된 항목 등) */
    private val _uiState = MutableStateFlow(ConversationUiState(isLoading = true))
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    /**
     * 이 대화방의 알림 목록 (페이징, 최신순).
     * LazyColumn에서 reverseLayout=true로 표시하므로 최신 메시지가 하단에 보임.
     * cachedIn으로 화면 재구성 시 데이터 유지.
     */
    val notifications = getNotificationsByConversationUseCase(packageName, conversationKey)
        .cachedIn(viewModelScope)

    init {
        Log.d(TAG, "init - packageName=$packageName, conversationKey=$conversationKey")

        // 화면 진입 즉시 이 대화방의 모든 알림을 읽음 처리
        // → DB isRead=1 업데이트 → Room이 변경 감지 → AppDetail/홈 unreadCount 자동 감소
        viewModelScope.launch { markConversationAsReadUseCase(packageName, conversationKey) }

        // isLoading 해제 및 타이틀(conversationKey) 설정
        _uiState.value = ConversationUiState(title = conversationKey, isLoading = false)
    }

    /**
     * 알림 항목 롱클릭 → 선택 모드 진입 + 해당 알림 ID 선택.
     */
    fun onItemLongClick(id: Long) {
        Log.d(TAG, "onItemLongClick() - id=$id")
        val selected = _uiState.value.selectedNotificationIds.toMutableSet()
        selected.add(id)
        _uiState.value = _uiState.value.copy(
            isSelectionMode = true,
            isAllSelected = false,
            selectedNotificationIds = selected
        )
    }

    /**
     * 알림 항목 클릭.
     * - 선택 모드가 아닌 경우: 아무 동작 없음
     * - 선택 모드인 경우: 해당 ID 선택 토글
     */
    fun onItemClick(id: Long) {
        if (!_uiState.value.isSelectionMode) return
        Log.d(TAG, "onItemClick() (선택 모드) - id=$id")
        toggleNotificationSelection(id)
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
            selectedNotificationIds = emptySet()
        )
    }

    /**
     * 전체 선택 / 전체 해제 토글.
     * 1. DB에서 이 대화방의 알림 ID 전체 목록 조회 (suspend)
     * 2. 현재 선택 목록과 비교 → 이미 전체 선택이면 해제, 아니면 전체 선택
     */
    fun selectAll() {
        viewModelScope.launch {
            val allIds = getAllNotificationIdsByConversationUseCase(packageName, conversationKey).toSet()
            val allSelected = allIds.isNotEmpty() &&
                _uiState.value.selectedNotificationIds.size == allIds.size &&
                _uiState.value.selectedNotificationIds.containsAll(allIds)
            val next = if (allSelected) emptySet() else allIds
            Log.d(TAG, "selectAll() - 전체=${allIds.size}개, 현재선택=${_uiState.value.selectedNotificationIds.size}개, allSelected=$allSelected")
            _uiState.value = _uiState.value.copy(
                isSelectionMode = true,
                isAllSelected = !allSelected && next.isNotEmpty(),
                selectedNotificationIds = next
            )
        }
    }

    /**
     * 선택된 알림들 삭제.
     * 삭제 완료 후 선택 모드 종료.
     */
    fun deleteSelected() {
        val targets = _uiState.value.selectedNotificationIds.toList()
        if (targets.isEmpty()) return
        Log.d(TAG, "deleteSelected() - 대상 id: $targets")
        viewModelScope.launch {
            deleteNotificationsByIdsUseCase(targets)
            Log.d(TAG, "deleteSelected() 완료")
            _uiState.value = _uiState.value.copy(
                isSelectionMode = false,
                isAllSelected = false,
                selectedNotificationIds = emptySet()
            )
        }
    }

    /**
     * 알림 ID 선택 토글.
     * - 이미 선택된 경우 → 제거
     * - 선택되지 않은 경우 → 추가
     */
    private fun toggleNotificationSelection(id: Long) {
        val selected = _uiState.value.selectedNotificationIds.toMutableSet()
        if (!selected.add(id)) selected.remove(id)
        Log.d(TAG, "toggleNotificationSelection() - id=$id, 현재선택: $selected")
        _uiState.value = _uiState.value.copy(
            isAllSelected = false,
            selectedNotificationIds = selected
        )
    }

    companion object {
        private const val TAG = "ConversationViewModel"
    }
}
