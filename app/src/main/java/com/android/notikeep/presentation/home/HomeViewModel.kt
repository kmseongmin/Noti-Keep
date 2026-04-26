package com.android.notikeep.presentation.home

import android.app.Notification
import android.util.Log
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

/**
 * 홈 화면 ViewModel.
 * 필터 변경 → 앱 목록 재로드, 선택 모드 관리, 선택 삭제를 담당.
 */
@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val getAllPackageNamesUseCase: GetAllPackageNamesUseCase,
    private val deleteNotificationsByPackagesUseCase: DeleteNotificationsByPackagesUseCase
) : ViewModel() {

    /**
     * 현재 선택된 필터를 저장하는 내부 StateFlow.
     * flatMapLatest의 upstream으로 사용 → 필터 변경 시 appGroups가 새 쿼리로 재구독됨.
     */
    private val _selectedFilter = MutableStateFlow(NotificationFilter.ALL)

    /** 화면 전반의 UI 상태 (선택 모드, 선택된 항목 등) */
    private val _uiState = MutableStateFlow(HomeUiState(selectedFilter = NotificationFilter.ALL))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /**
     * 홈 화면에 표시할 앱 그룹 목록 (페이징).
     *
     * 동작 흐름:
     * 1. _selectedFilter가 변경되면 flatMapLatest가 이전 Flow를 취소하고 새 Flow를 구독
     * 2. getNotificationsUseCase(category)로 DB 쿼리 실행
     * 3. PagingData<AppGroupSummary> → PagingData<AppGroup>으로 매핑 (presentation 모델로 변환)
     * 4. cachedIn(viewModelScope)으로 화면 회전 등 재구독 시 캐시 재사용
     */
    val appGroups = _selectedFilter
        .flatMapLatest { filter ->
            Log.d(TAG, "appGroups 재구독 - filter=${filter.name}")
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

    /**
     * 필터 변경.
     * _selectedFilter 업데이트 → appGroups flatMapLatest 재실행.
     * uiState.selectedFilter도 동기화해 FilterChip 선택 상태 유지.
     */
    fun setFilter(filter: NotificationFilter) {
        Log.d(TAG, "setFilter() - ${filter.name}")
        _selectedFilter.value = filter
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    /**
     * 항목 롱클릭 → 선택 모드 진입 + 해당 패키지 선택.
     * isSelectionMode가 false였던 경우에도 강제로 true로 전환.
     */
    fun onItemLongClick(packageName: String) {
        Log.d(TAG, "onItemLongClick() - packageName=$packageName")
        val selected = _uiState.value.selectedPackageNames.toMutableSet()
        selected.add(packageName)
        _uiState.value = _uiState.value.copy(
            isSelectionMode = true,
            isAllSelected = false,
            selectedPackageNames = selected
        )
    }

    /**
     * 항목 클릭.
     * - 선택 모드가 아닌 경우: HomeScreen에서 onAppClick 콜백 직접 처리 (AppDetail 이동)
     * - 선택 모드인 경우: 해당 패키지 선택 토글
     */
    fun onItemClick(packageName: String) {
        if (!_uiState.value.isSelectionMode) return
        Log.d(TAG, "onItemClick() (선택 모드) - packageName=$packageName")
        togglePackageSelection(packageName)
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
            selectedPackageNames = emptySet()
        )
    }

    /**
     * 전체 선택 / 전체 해제 토글.
     * 1. DB에서 현재 패키지 전체 목록 조회 (suspend)
     * 2. 현재 선택 목록과 비교 → 이미 전체 선택이면 해제, 아니면 전체 선택
     */
    fun selectAll() {
        viewModelScope.launch {
            val allPackageNames = getAllPackageNamesUseCase(_selectedFilter.value.toCategory()).toSet()
            val allSelected = allPackageNames.isNotEmpty() &&
                _uiState.value.selectedPackageNames.size == allPackageNames.size &&
                _uiState.value.selectedPackageNames.containsAll(allPackageNames)
            val next = if (allSelected) emptySet() else allPackageNames
            Log.d(TAG, "selectAll() - 전체=${allPackageNames.size}개, 현재선택=${_uiState.value.selectedPackageNames.size}개, allSelected=$allSelected")
            _uiState.value = _uiState.value.copy(
                isSelectionMode = true,
                isAllSelected = !allSelected && next.isNotEmpty(),
                selectedPackageNames = next
            )
        }
    }

    /**
     * 선택된 패키지들의 알림 전체 삭제.
     * 삭제 완료 후 선택 모드 종료.
     */
    fun deleteSelected() {
        val targets = _uiState.value.selectedPackageNames.toList()
        if (targets.isEmpty()) return
        Log.d(TAG, "deleteSelected() - 대상: $targets")
        viewModelScope.launch {
            deleteNotificationsByPackagesUseCase(targets)
            Log.d(TAG, "deleteSelected() 완료")
            _uiState.value = _uiState.value.copy(
                isSelectionMode = false,
                isAllSelected = false,
                selectedPackageNames = emptySet()
            )
        }
    }

    /**
     * 패키지 선택 토글.
     * - 이미 선택된 경우 → 제거
     * - 선택되지 않은 경우 → 추가
     * isAllSelected는 false로 리셋 (전체 선택 상태 해제)
     */
    private fun togglePackageSelection(packageName: String) {
        val selected = _uiState.value.selectedPackageNames.toMutableSet()
        // add()가 false를 반환하면 이미 있는 항목 → remove
        if (!selected.add(packageName)) selected.remove(packageName)
        Log.d(TAG, "togglePackageSelection() - $packageName, 현재선택: $selected")
        _uiState.value = _uiState.value.copy(
            isAllSelected = false,
            selectedPackageNames = selected
        )
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}

/**
 * NotificationFilter → DB 쿼리용 category 문자열 변환.
 * ALL → null (전체 조회), MESSAGE → "msg" 카테고리 문자열
 */
private fun NotificationFilter.toCategory(): String? = when (this) {
    NotificationFilter.ALL -> null
    NotificationFilter.MESSAGE -> Notification.CATEGORY_MESSAGE
}
