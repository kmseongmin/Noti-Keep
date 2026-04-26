package com.android.notikeep.presentation.home

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Badge
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.android.notikeep.presentation.ui.component.AppIcon
import com.android.notikeep.presentation.ui.component.SelectionActionBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 홈 화면 Stateful 컴포저블.
 * ViewModel에서 상태를 수집해 Stateless인 HomeContent에 전달.
 *
 * @param onAppClick 앱 항목 클릭 콜백. packageName을 받아 NavGraph에서 AppDetail로 이동
 */
@Composable
fun HomeScreen(
    onAppClick: (packageName: String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    // uiState: 선택 모드, 필터 등 화면 상태. Lifecycle에 맞춰 구독
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // appGroups: 페이징 데이터. LazyPagingItems로 변환해 LazyColumn에 전달
    val appGroups = viewModel.appGroups.collectAsLazyPagingItems()
    HomeContent(
        uiState = uiState,
        appGroups = appGroups,
        onAppClick = onAppClick,
        onFilterSelect = viewModel::setFilter,
        onItemClick = viewModel::onItemClick,
        onItemLongClick = viewModel::onItemLongClick,
        onSelectAll = viewModel::selectAll,
        onDeleteSelected = viewModel::deleteSelected,
        onClearSelection = viewModel::clearSelection
    )
}

/**
 * 홈 화면 Stateless 컴포저블.
 * 상태와 이벤트 람다를 파라미터로 받아 UI만 담당.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    uiState: HomeUiState,
    appGroups: LazyPagingItems<AppGroup>,
    onAppClick: (packageName: String) -> Unit,
    onFilterSelect: (NotificationFilter) -> Unit,
    onItemClick: (packageName: String) -> Unit,
    onItemLongClick: (packageName: String) -> Unit,
    onSelectAll: () -> Unit,
    onDeleteSelected: () -> Unit,
    onClearSelection: () -> Unit
) {
    // 선택 모드일 때 시스템 뒤로가기 → 선택 모드 종료
    BackHandler(enabled = uiState.isSelectionMode) {
        onClearSelection()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("알림 목록") },
                actions = { DebugActions() }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 선택 모드일 때만 상단에 액션바 표시
            if (uiState.isSelectionMode) {
                item {
                    SelectionActionBar(
                        selectedCount = uiState.selectedPackageNames.size,
                        isAllSelected = uiState.isAllSelected,
                        onToggleSelectAll = onSelectAll,
                        onDeleteSelected = onDeleteSelected,
                        onClearSelection = onClearSelection
                    )
                }
            }
            // 필터 칩 행 (전체 / 메시지)
            item {
                FilterChipRow(
                    selectedFilter = uiState.selectedFilter,
                    onFilterSelect = onFilterSelect
                )
            }
            // 앱 그룹 목록 (페이징)
            items(
                count = appGroups.itemCount,
                key = appGroups.itemKey { it.packageName }  // packageName을 안정적 key로 사용
            ) { index ->
                val group = appGroups[index] ?: return@items  // 아직 로드 안 된 항목은 스킵
                AppGroupItem(
                    group = group,
                    isSelectionMode = uiState.isSelectionMode,
                    isSelected = uiState.selectedPackageNames.contains(group.packageName),
                    onClick = {
                        // 선택 모드: 선택 토글 / 일반 모드: AppDetail 이동
                        if (uiState.isSelectionMode) onItemClick(group.packageName)
                        else onAppClick(group.packageName)
                    },
                    onLongClick = { onItemLongClick(group.packageName) }
                )
            }

            // 페이징 로딩 인디케이터
            if (appGroups.loadState.append is LoadState.Loading) {
                item { PagingLoadingItem() }
            }
            if (appGroups.loadState.prepend is LoadState.Loading) {
                item { PagingLoadingItem() }
            }
            if (appGroups.loadState.refresh is LoadState.Loading) {
                item { PagingLoadingItem() }
            }
        }
    }
}

/**
 * 필터 칩 가로 스크롤 행.
 * NotificationFilter.entries(ALL, MESSAGE)를 칩으로 표시.
 *
 * @param selectedFilter 현재 선택된 필터
 * @param onFilterSelect 칩 클릭 콜백
 */
@Composable
fun FilterChipRow(
    selectedFilter: NotificationFilter,
    onFilterSelect: (NotificationFilter) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(NotificationFilter.entries) { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelect(filter) },
                label = { Text(filter.label) }
            )
        }
    }
}

/**
 * 앱 그룹 단일 항목 UI.
 * 앱 아이콘 + 앱 이름 + 안읽은 배지 + 최신 알림 미리보기 + 시간 표시.
 *
 * @param group 표시할 앱 그룹 데이터
 * @param isSelectionMode 선택 모드 여부 (true면 체크박스 표시)
 * @param isSelected 현재 선택 여부
 * @param onClick 클릭 콜백
 * @param onLongClick 롱클릭 콜백 (선택 모드 진입)
 */
@Composable
fun AppGroupItem(
    group: AppGroup,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 선택 모드일 때만 체크박스 표시
        if (isSelectionMode) {
            Checkbox(checked = isSelected, onCheckedChange = { onClick() })
        }
        // PackageManager에서 앱 아이콘 로드
        AppIcon(packageName = group.packageName)

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 앱 이름
                    Text(
                        text = group.appName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    // 안읽은 알림이 있을 때만 배지 표시
                    if (group.unreadCount > 0) {
                        Badge { Text(group.unreadCount.toString()) }
                    }
                }
                // 최근 알림 수신 시각
                Text(
                    text = formatTime(group.latestReceivedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            // 최근 알림 발신자 이름 (1줄, 넘치면 말줄임)
            Text(
                text = group.latestTitle,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
            // 최근 알림 본문 미리보기 (1줄, 넘치면 말줄임)
            Text(
                text = group.latestContent,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

/** 밀리초 타임스탬프 → "MM.dd HH:mm" 형식 문자열 변환 */
private fun formatTime(millis: Long): String =
    SimpleDateFormat("MM.dd HH:mm", Locale.getDefault()).format(Date(millis))

/** 페이징 추가 로드 중 표시되는 로딩 인디케이터 */
@Composable
private fun PagingLoadingItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
    }
}

// ─────────────────────────────────────────
// Preview
// ─────────────────────────────────────────

private val previewAppGroups = listOf(
    AppGroup(
        packageName = "com.kakao.talk",
        appName = "카카오톡",
        latestTitle = "홍길동",
        latestContent = "내일 회의 몇 시예요?",
        latestCategory = "msg",
        latestReceivedAt = System.currentTimeMillis() - 60_000,
        count = 10,
        unreadCount = 3
    ),
    AppGroup(
        packageName = "com.instagram.android",
        appName = "Instagram",
        latestTitle = "user123님이 회원님의 게시물에 좋아요를 눌렀습니다.",
        latestContent = "",
        latestCategory = null,
        latestReceivedAt = System.currentTimeMillis() - 3_600_000,
        count = 2,
        unreadCount = 0
    ),
    AppGroup(
        packageName = "com.slack",
        appName = "Slack",
        latestTitle = "김개발",
        latestContent = "PR 리뷰 부탁드립니다 🙏",
        latestCategory = "msg",
        latestReceivedAt = System.currentTimeMillis() - 7_200_000,
        count = 5,
        unreadCount = 5
    )
)

/** 홈 화면 전체 프리뷰 - 일반 모드 */
@Preview(showBackground = true, name = "홈 화면")
@Composable
private fun HomeContentPreview() {
    val fakePagingItems = androidx.paging.compose.collectAsLazyPagingItems(
        kotlinx.coroutines.flow.flowOf(androidx.paging.PagingData.from(previewAppGroups))
    )
    com.android.notikeep.presentation.ui.theme.NotiKeepTheme {
        HomeContent(
            uiState = HomeUiState(selectedFilter = NotificationFilter.ALL),
            appGroups = fakePagingItems,
            onAppClick = {},
            onFilterSelect = {},
            onItemClick = {},
            onItemLongClick = {},
            onSelectAll = {},
            onDeleteSelected = {},
            onClearSelection = {}
        )
    }
}

/** 홈 화면 전체 프리뷰 - 선택 모드 */
@Preview(showBackground = true, name = "홈 화면 - 선택 모드")
@Composable
private fun HomeContentSelectionModePreview() {
    val fakePagingItems = androidx.paging.compose.collectAsLazyPagingItems(
        kotlinx.coroutines.flow.flowOf(androidx.paging.PagingData.from(previewAppGroups))
    )
    com.android.notikeep.presentation.ui.theme.NotiKeepTheme {
        HomeContent(
            uiState = HomeUiState(
                selectedFilter = NotificationFilter.ALL,
                isSelectionMode = true,
                isAllSelected = false,
                selectedPackageNames = setOf("com.kakao.talk")
            ),
            appGroups = fakePagingItems,
            onAppClick = {},
            onFilterSelect = {},
            onItemClick = {},
            onItemLongClick = {},
            onSelectAll = {},
            onDeleteSelected = {},
            onClearSelection = {}
        )
    }
}

/** 홈 화면 전체 프리뷰 - 메시지 필터 */
@Preview(showBackground = true, name = "홈 화면 - 메시지 필터")
@Composable
private fun HomeContentMessageFilterPreview() {
    val fakePagingItems = androidx.paging.compose.collectAsLazyPagingItems(
        kotlinx.coroutines.flow.flowOf(
            androidx.paging.PagingData.from(previewAppGroups.filter { it.latestCategory == "msg" })
        )
    )
    com.android.notikeep.presentation.ui.theme.NotiKeepTheme {
        HomeContent(
            uiState = HomeUiState(selectedFilter = NotificationFilter.MESSAGE),
            appGroups = fakePagingItems,
            onAppClick = {},
            onFilterSelect = {},
            onItemClick = {},
            onItemLongClick = {},
            onSelectAll = {},
            onDeleteSelected = {},
            onClearSelection = {}
        )
    }
}

/** 필터 칩 행 프리뷰 - ALL 선택 */
@Preview(showBackground = true, name = "필터 칩 - ALL")
@Composable
private fun FilterChipRowAllPreview() {
    com.android.notikeep.presentation.ui.theme.NotiKeepTheme {
        FilterChipRow(selectedFilter = NotificationFilter.ALL, onFilterSelect = {})
    }
}

/** 필터 칩 행 프리뷰 - MESSAGE 선택 */
@Preview(showBackground = true, name = "필터 칩 - MESSAGE")
@Composable
private fun FilterChipRowMessagePreview() {
    com.android.notikeep.presentation.ui.theme.NotiKeepTheme {
        FilterChipRow(selectedFilter = NotificationFilter.MESSAGE, onFilterSelect = {})
    }
}

/** 앱 그룹 항목 프리뷰 - 안읽은 배지 있음 */
@Preview(showBackground = true, name = "앱 그룹 항목 - 안읽음 배지")
@Composable
private fun AppGroupItemUnreadPreview() {
    com.android.notikeep.presentation.ui.theme.NotiKeepTheme {
        AppGroupItem(
            group = previewAppGroups[0],
            isSelectionMode = false,
            isSelected = false,
            onClick = {},
            onLongClick = {}
        )
    }
}

/** 앱 그룹 항목 프리뷰 - 안읽은 배지 없음 */
@Preview(showBackground = true, name = "앱 그룹 항목 - 읽음")
@Composable
private fun AppGroupItemReadPreview() {
    com.android.notikeep.presentation.ui.theme.NotiKeepTheme {
        AppGroupItem(
            group = previewAppGroups[1],
            isSelectionMode = false,
            isSelected = false,
            onClick = {},
            onLongClick = {}
        )
    }
}

/** 앱 그룹 항목 프리뷰 - 선택 모드 (선택됨) */
@Preview(showBackground = true, name = "앱 그룹 항목 - 선택됨")
@Composable
private fun AppGroupItemSelectedPreview() {
    com.android.notikeep.presentation.ui.theme.NotiKeepTheme {
        AppGroupItem(
            group = previewAppGroups[0],
            isSelectionMode = true,
            isSelected = true,
            onClick = {},
            onLongClick = {}
        )
    }
}

/** 앱 그룹 항목 프리뷰 - 선택 모드 (미선택) */
@Preview(showBackground = true, name = "앱 그룹 항목 - 선택 모드 미선택")
@Composable
private fun AppGroupItemUnselectedPreview() {
    com.android.notikeep.presentation.ui.theme.NotiKeepTheme {
        AppGroupItem(
            group = previewAppGroups[2],
            isSelectionMode = true,
            isSelected = false,
            onClick = {},
            onLongClick = {}
        )
    }
}
