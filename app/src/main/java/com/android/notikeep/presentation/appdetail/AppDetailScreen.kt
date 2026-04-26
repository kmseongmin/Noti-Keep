package com.android.notikeep.presentation.appdetail

import android.app.Notification
import androidx.compose.ui.tooling.preview.Preview
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.android.notikeep.presentation.ui.component.SelectionActionBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 앱 상세 화면 Stateful 컴포저블.
 * 특정 앱의 대화방 목록을 표시.
 *
 * @param onBack 뒤로가기 콜백
 * @param onConversationClick 메시지 대화방 클릭 콜백. packageName, conversationKey를 받아 Conversation으로 이동
 */
@Composable
fun AppDetailScreen(
    onBack: () -> Unit,
    onConversationClick: (packageName: String, conversationKey: String) -> Unit,
    viewModel: AppDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val conversations = viewModel.conversations.collectAsLazyPagingItems()
    AppDetailContent(
        uiState = uiState,
        conversations = conversations,
        onBack = onBack,
        onConversationClick = onConversationClick,
        onItemClick = viewModel::onItemClick,
        onItemLongClick = viewModel::onItemLongClick,
        onSelectAll = viewModel::selectAll,
        onDeleteSelected = viewModel::deleteSelected,
        onClearSelection = viewModel::clearSelection
    )
}

/**
 * 앱 상세 화면 Stateless 컴포저블.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailContent(
    uiState: AppDetailUiState,
    conversations: LazyPagingItems<ConversationGroup>,
    onBack: () -> Unit,
    onConversationClick: (packageName: String, conversationKey: String) -> Unit,
    onItemClick: (conversationKey: String) -> Unit,
    onItemLongClick: (conversationKey: String) -> Unit,
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
                title = { Text(uiState.appName) },
                navigationIcon = {
                    IconButton(onClick = {
                        // 선택 모드일 때 뒤로가기 아이콘 → 선택 모드 종료 (앱 이전 화면 이동 X)
                        if (uiState.isSelectionMode) onClearSelection() else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
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
                        selectedCount = uiState.selectedConversationKeys.size,
                        isAllSelected = uiState.isAllSelected,
                        onToggleSelectAll = onSelectAll,
                        onDeleteSelected = onDeleteSelected,
                        onClearSelection = onClearSelection
                    )
                }
            }
            items(
                count = conversations.itemCount,
                key = conversations.itemKey { it.conversationKey }
            ) { index ->
                val conversation = conversations[index] ?: return@items
                // 메시지 카테고리인 경우에만 Conversation 화면으로 진입 가능
                val isMessaging = conversation.latest.category == Notification.CATEGORY_MESSAGE
                ConversationGroupItem(
                    conversation = conversation,
                    isMessaging = isMessaging,
                    isSelectionMode = uiState.isSelectionMode,
                    isSelected = uiState.selectedConversationKeys.contains(conversation.conversationKey),
                    onClick = if (isMessaging) {
                        {
                            if (uiState.isSelectionMode) {
                                onItemClick(conversation.conversationKey)
                            } else {
                                // 메시지 항목 클릭 → Conversation 화면으로 이동
                                onConversationClick(
                                    conversation.latest.packageName,
                                    conversation.conversationKey
                                )
                            }
                        }
                    } else if (uiState.isSelectionMode) {
                        // 비메시지 항목도 선택 모드에서는 선택 토글 가능
                        { onItemClick(conversation.conversationKey) }
                    } else null,  // 비메시지 + 일반 모드: 클릭 반응 없음
                    onLongClick = { onItemLongClick(conversation.conversationKey) }
                )
            }

            if (conversations.loadState.append is LoadState.Loading) {
                item { PagingLoadingItem() }
            }
            if (conversations.loadState.prepend is LoadState.Loading) {
                item { PagingLoadingItem() }
            }
            if (conversations.loadState.refresh is LoadState.Loading) {
                item { PagingLoadingItem() }
            }
        }
    }
}

/**
 * 대화방 그룹 단일 항목 UI.
 * 대화방 이름 + 안읽은 배지 + 최신 메시지 미리보기 + 시간 + 화살표(메시지일 때).
 *
 * @param conversation 표시할 대화방 데이터
 * @param isMessaging 메시지 카테고리 여부 (화살표 아이콘 표시, 클릭 활성화 기준)
 * @param isSelectionMode 선택 모드 여부
 * @param isSelected 현재 선택 여부
 * @param onClick 클릭 콜백. null이면 클릭 비활성화
 * @param onLongClick 롱클릭 콜백
 */
@Composable
fun ConversationGroupItem(
    conversation: ConversationGroup,
    isMessaging: Boolean,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: (() -> Unit)?,
    onLongClick: () -> Unit
) {
    val modifier = Modifier
        .fillMaxWidth()
        .combinedClickable(
            onClick = { onClick?.invoke() },
            onLongClick = onLongClick
        )
        .padding(horizontal = 16.dp, vertical = 10.dp)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (isSelectionMode) {
                    Checkbox(checked = isSelected, onCheckedChange = { onClick?.invoke() })
                }
                // 대화방 이름 (단체방 이름 또는 발신자 이름)
                Text(
                    text = conversation.conversationKey,
                    style = MaterialTheme.typography.bodyMedium
                )
                // 메시지이고 안읽은 알림이 있을 때만 배지 표시
                if (isMessaging && conversation.unreadCount > 0) {
                    Badge { Text(conversation.unreadCount.toString()) }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 최근 알림 수신 시각
                Text(
                    text = formatTime(conversation.latest.receivedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                // 메시지 항목에만 우측 화살표 아이콘 표시 (클릭 가능 힌트)
                if (isMessaging) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
        // 단톡방일 때 최근 발신자 이름 표시 (subText != null)
        if (conversation.latest.subText?.isNotBlank() == true) {
            Text(
                text = conversation.latest.title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        // 최근 메시지 본문 미리보기
        Text(
            text = conversation.latest.content,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
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

private fun makeNotification(
    title: String,
    content: String,
    subText: String? = null,
    category: String? = Notification.CATEGORY_MESSAGE
) = com.android.notikeep.domain.model.AppNotification(
    packageName = "com.kakao.talk",
    appName = "카카오톡",
    title = title,
    content = content,
    subText = subText,
    category = category,
    receivedAt = System.currentTimeMillis() - 120_000
)

private val previewConversations = listOf(
    ConversationGroup(
        conversationKey = "개발팀 단톡",
        latest = makeNotification("홍길동", "PR 리뷰 부탁드립니다 🙏", subText = "개발팀 단톡"),
        count = 20,
        unreadCount = 5
    ),
    ConversationGroup(
        conversationKey = "김영희",
        latest = makeNotification("김영희", "오늘 점심 같이 먹을까요?"),
        count = 8,
        unreadCount = 0
    ),
    ConversationGroup(
        conversationKey = "디자인 피드백",
        latest = makeNotification("박민준", "수정사항 확인해주세요", subText = "디자인 피드백"),
        count = 3,
        unreadCount = 3
    ),
    ConversationGroup(
        conversationKey = "Instagram",
        latest = makeNotification("Instagram", "홍길동님이 회원님을 팔로우하기 시작했습니다.", category = null),
        count = 1,
        unreadCount = 1
    )
)

/** AppDetail 화면 전체 프리뷰 - 일반 모드 */
@Preview(showBackground = true, name = "AppDetail 화면")
@Composable
private fun AppDetailContentPreview() {
    val fakePagingItems = androidx.paging.compose.collectAsLazyPagingItems(
        kotlinx.coroutines.flow.flowOf(androidx.paging.PagingData.from(previewConversations))
    )
    com.android.notikeep.presentation.ui.theme.NotiKeepTheme {
        AppDetailContent(
            uiState = AppDetailUiState(appName = "카카오톡", isLoading = false),
            conversations = fakePagingItems,
            onBack = {},
            onConversationClick = { _, _ -> },
            onItemClick = {},
            onItemLongClick = {},
            onSelectAll = {},
            onDeleteSelected = {},
            onClearSelection = {}
        )
    }
}

/** AppDetail 화면 전체 프리뷰 - 선택 모드 */
@Preview(showBackground = true, name = "AppDetail 화면 - 선택 모드")
@Composable
private fun AppDetailContentSelectionModePreview() {
    val fakePagingItems = androidx.paging.compose.collectAsLazyPagingItems(
        kotlinx.coroutines.flow.flowOf(androidx.paging.PagingData.from(previewConversations))
    )
    com.android.notikeep.presentation.ui.theme.NotiKeepTheme {
        AppDetailContent(
            uiState = AppDetailUiState(
                appName = "카카오톡",
                isLoading = false,
                isSelectionMode = true,
                isAllSelected = false,
                selectedConversationKeys = setOf("개발팀 단톡")
            ),
            conversations = fakePagingItems,
            onBack = {},
            onConversationClick = { _, _ -> },
            onItemClick = {},
            onItemLongClick = {},
            onSelectAll = {},
            onDeleteSelected = {},
            onClearSelection = {}
        )
    }
}

/** 대화방 항목 프리뷰 - 단체 채팅방, 안읽음 배지 */
@Preview(showBackground = true, name = "대화방 항목 - 단체방 안읽음")
@Composable
private fun ConversationGroupItemGroupUnreadPreview() {
    com.android.notikeep.presentation.ui.theme.NotiKeepTheme {
        ConversationGroupItem(
            conversation = previewConversations[0],
            isMessaging = true,
            isSelectionMode = false,
            isSelected = false,
            onClick = {},
            onLongClick = {}
        )
    }
}

/** 대화방 항목 프리뷰 - 1:1 채팅, 읽음 */
@Preview(showBackground = true, name = "대화방 항목 - 1:1 읽음")
@Composable
private fun ConversationGroupItemDirectReadPreview() {
    com.android.notikeep.presentation.ui.theme.NotiKeepTheme {
        ConversationGroupItem(
            conversation = previewConversations[1],
            isMessaging = true,
            isSelectionMode = false,
            isSelected = false,
            onClick = {},
            onLongClick = {}
        )
    }
}

/** 대화방 항목 프리뷰 - 비메시지 카테고리 */
@Preview(showBackground = true, name = "대화방 항목 - 비메시지")
@Composable
private fun ConversationGroupItemNonMessagingPreview() {
    com.android.notikeep.presentation.ui.theme.NotiKeepTheme {
        ConversationGroupItem(
            conversation = previewConversations[3],
            isMessaging = false,
            isSelectionMode = false,
            isSelected = false,
            onClick = null,
            onLongClick = {}
        )
    }
}

/** 대화방 항목 프리뷰 - 선택 모드 (선택됨) */
@Preview(showBackground = true, name = "대화방 항목 - 선택됨")
@Composable
private fun ConversationGroupItemSelectedPreview() {
    com.android.notikeep.presentation.ui.theme.NotiKeepTheme {
        ConversationGroupItem(
            conversation = previewConversations[0],
            isMessaging = true,
            isSelectionMode = true,
            isSelected = true,
            onClick = {},
            onLongClick = {}
        )
    }
}

/** 대화방 항목 프리뷰 - 선택 모드 (미선택) */
@Preview(showBackground = true, name = "대화방 항목 - 선택 모드 미선택")
@Composable
private fun ConversationGroupItemUnselectedPreview() {
    com.android.notikeep.presentation.ui.theme.NotiKeepTheme {
        ConversationGroupItem(
            conversation = previewConversations[2],
            isMessaging = true,
            isSelectionMode = true,
            isSelected = false,
            onClick = {},
            onLongClick = {}
        )
    }
}
