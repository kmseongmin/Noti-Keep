package com.android.notikeep.presentation.conversation

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import com.android.notikeep.domain.model.AppNotification
import com.android.notikeep.presentation.ui.component.SelectionActionBar
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 대화 내역 화면 Stateful 컴포저블.
 * 특정 대화방의 알림을 메시지 버블 형태로 표시.
 * 화면 진입 시 ConversationViewModel.init에서 자동 읽음 처리.
 *
 * @param onBack 뒤로가기 콜백
 */
@Composable
fun ConversationScreen(
    onBack: () -> Unit,
    viewModel: ConversationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val notifications = viewModel.notifications.collectAsLazyPagingItems()
    ConversationContent(
        uiState = uiState,
        notifications = notifications,
        onBack = onBack,
        onItemClick = viewModel::onItemClick,
        onItemLongClick = viewModel::onItemLongClick,
        onSelectAll = viewModel::selectAll,
        onDeleteSelected = viewModel::deleteSelected,
        onClearSelection = viewModel::clearSelection
    )
}

/**
 * 대화 내역 화면 Stateless 컴포저블.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationContent(
    uiState: ConversationUiState,
    notifications: LazyPagingItems<AppNotification>,
    onBack: () -> Unit,
    onItemClick: (Long) -> Unit,
    onItemLongClick: (Long) -> Unit,
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
                title = { Text(uiState.title) },  // 대화방 이름 또는 발신자 이름
                navigationIcon = {
                    IconButton(onClick = {
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            // reverseLayout=true: 목록을 아래→위 순으로 배치
            // 페이징 데이터는 최신순(DESC)이므로 reverseLayout으로 최신 메시지가 하단에 위치
            reverseLayout = true
        ) {
            // 선택 모드일 때만 상단(reverseLayout에서는 하단) 액션바 표시
            if (uiState.isSelectionMode) {
                item {
                    SelectionActionBar(
                        selectedCount = uiState.selectedNotificationIds.size,
                        isAllSelected = uiState.isAllSelected,
                        onToggleSelectAll = onSelectAll,
                        onDeleteSelected = onDeleteSelected,
                        onClearSelection = onClearSelection
                    )
                }
            }
            items(
                count = notifications.itemCount,
                key = notifications.itemKey { it.id }  // 알림 ID를 안정적 key로 사용
            ) { index ->
                val notification = notifications[index] ?: return@items
                MessageBubble(
                    notification = notification,
                    isSelectionMode = uiState.isSelectionMode,
                    isSelected = uiState.selectedNotificationIds.contains(notification.id),
                    onClick = {
                        // 선택 모드일 때만 클릭으로 선택 토글
                        if (uiState.isSelectionMode) onItemClick(notification.id)
                    },
                    onLongClick = { onItemLongClick(notification.id) }
                )
            }

            if (notifications.loadState.append is LoadState.Loading) {
                item { PagingLoadingItem() }
            }
            if (notifications.loadState.prepend is LoadState.Loading) {
                item { PagingLoadingItem() }
            }
            if (notifications.loadState.refresh is LoadState.Loading) {
                item { PagingLoadingItem() }
            }
        }
    }
}

/**
 * 메시지 버블 단일 항목 UI.
 * 발신자 아이콘 + (단체방일 때) 발신자 이름 + 메시지 카드 + 시간 표시.
 *
 * @param notification 표시할 알림 데이터
 * @param isSelectionMode 선택 모드 여부
 * @param isSelected 현재 선택 여부
 * @param onClick 클릭 콜백
 * @param onLongClick 롱클릭 콜백
 */
@Composable
fun MessageBubble(
    notification: AppNotification,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    // subText가 있으면 단체 채팅방 → 발신자 아이콘과 이름 표시
    val isGroupChat = notification.subText?.isNotBlank() == true
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onClick() },
                modifier = Modifier.padding(top = 6.dp)
            )
        }
        // 단체 채팅방이거나 발신자 프로필 이미지가 있을 때 아이콘 표시
        if (isGroupChat || notification.senderIconPath != null) {
            SenderIcon(
                senderIconPath = notification.senderIconPath,
                senderName = notification.title
            )
        }
        Column(modifier = Modifier.widthIn(max = 260.dp)) {
            // 단체 채팅방일 때만 발신자 이름 표시
            if (isGroupChat) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }
            // 메시지 본문 카드
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = notification.content,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
            // 수신 시각
            Text(
                text = formatTime(notification.receivedAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

/**
 * 발신자 프로필 아이콘.
 * - 저장된 이미지 파일이 존재하면 Coil로 원형 표시
 * - 없으면 이름 첫 글자를 이니셜로 표시 (폴백)
 *
 * @param senderIconPath 내부 저장소의 프로필 이미지 경로. null이면 이니셜 폴백
 * @param senderName 발신자 이름. 이니셜 표시 및 contentDescription에 사용
 */
@Composable
fun SenderIcon(senderIconPath: String?, senderName: String) {
    val iconFile = senderIconPath?.let { File(it) }
    if (iconFile != null && iconFile.exists()) {
        // 저장된 프로필 이미지 표시
        AsyncImage(
            model = iconFile,
            contentDescription = senderName,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        // 이미지 없을 때 이름 첫 글자 이니셜 표시
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = senderName.take(1),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
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
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
    }
}

// ─────────────────────────────────────────
// Preview
// ─────────────────────────────────────────

private fun makeMsg(
    id: Long,
    title: String,
    content: String,
    subText: String? = null,
    offsetMs: Long = 0
) = com.android.notikeep.domain.model.AppNotification(
    id = id,
    packageName = "com.kakao.talk",
    appName = "카카오톡",
    title = title,
    content = content,
    subText = subText,
    category = android.app.Notification.CATEGORY_MESSAGE,
    receivedAt = System.currentTimeMillis() - offsetMs,
    isRead = true
)

private val previewNotifications = listOf(
    makeMsg(1L, "홍길동", "안녕하세요! 오늘 저녁 같이 먹을까요?", offsetMs = 300_000),
    makeMsg(2L, "홍길동", "괜찮으시면 7시에 홍대 어떠세요?", offsetMs = 240_000),
    makeMsg(3L, "홍길동", "네 좋아요! 장소 공유해 드릴게요 😊", offsetMs = 180_000),
    makeMsg(4L, "홍길동", "이 식당 어때요? 후기 좋던데", offsetMs = 60_000),
    makeMsg(5L, "홍길동", "확인했어요! 거기로 가요", offsetMs = 30_000)
)

private val previewGroupNotifications = listOf(
    makeMsg(1L, "김팀장", "오늘 스프린트 회의 3시로 변경됩니다", subText = "개발팀 단톡", offsetMs = 600_000),
    makeMsg(2L, "박디자인", "UI 시안 업로드했으니 확인해주세요", subText = "개발팀 단톡", offsetMs = 300_000),
    makeMsg(3L, "이백엔드", "API 연동 완료! PR 올렸습니다", subText = "개발팀 단톡", offsetMs = 120_000),
    makeMsg(4L, "홍길동", "수고하셨습니다 🎉", subText = "개발팀 단톡", offsetMs = 30_000)
)

/** Conversation 화면 전체 프리뷰 - 1:1 채팅 */
@Preview(showBackground = true, name = "Conversation 화면 - 1:1 채팅")
@Composable
private fun ConversationContentDirectPreview() {
    val fakePagingItems = androidx.paging.compose.collectAsLazyPagingItems(
        kotlinx.coroutines.flow.flowOf(androidx.paging.PagingData.from(previewNotifications))
    )
    com.android.notikeep.presentation.ui.theme.NotiKeepTheme {
        ConversationContent(
            uiState = ConversationUiState(title = "홍길동", isLoading = false),
            notifications = fakePagingItems,
            onBack = {},
            onItemClick = {},
            onItemLongClick = {},
            onSelectAll = {},
            onDeleteSelected = {},
            onClearSelection = {}
        )
    }
}

/** Conversation 화면 전체 프리뷰 - 단체 채팅 */
@Preview(showBackground = true, name = "Conversation 화면 - 단체 채팅")
@Composable
private fun ConversationContentGroupPreview() {
    val fakePagingItems = androidx.paging.compose.collectAsLazyPagingItems(
        kotlinx.coroutines.flow.flowOf(androidx.paging.PagingData.from(previewGroupNotifications))
    )
    com.android.notikeep.presentation.ui.theme.NotiKeepTheme {
        ConversationContent(
            uiState = ConversationUiState(title = "개발팀 단톡", isLoading = false),
            notifications = fakePagingItems,
            onBack = {},
            onItemClick = {},
            onItemLongClick = {},
            onSelectAll = {},
            onDeleteSelected = {},
            onClearSelection = {}
        )
    }
}

/** Conversation 화면 전체 프리뷰 - 선택 모드 */
@Preview(showBackground = true, name = "Conversation 화면 - 선택 모드")
@Composable
private fun ConversationContentSelectionModePreview() {
    val fakePagingItems = androidx.paging.compose.collectAsLazyPagingItems(
        kotlinx.coroutines.flow.flowOf(androidx.paging.PagingData.from(previewNotifications))
    )
    com.android.notikeep.presentation.ui.theme.NotiKeepTheme {
        ConversationContent(
            uiState = ConversationUiState(
                title = "홍길동",
                isLoading = false,
                isSelectionMode = true,
                selectedNotificationIds = setOf(1L, 3L)
            ),
            notifications = fakePagingItems,
            onBack = {},
            onItemClick = {},
            onItemLongClick = {},
            onSelectAll = {},
            onDeleteSelected = {},
            onClearSelection = {}
        )
    }
}

/** 메시지 버블 프리뷰 - 1:1 채팅 (프로필 없음) */
@Preview(showBackground = true, name = "메시지 버블 - 1:1 채팅")
@Composable
private fun MessageBubbleDirectPreview() {
    com.android.notikeep.presentation.ui.theme.NotiKeepTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MessageBubble(
                notification = previewNotifications[0],
                isSelectionMode = false,
                isSelected = false,
                onClick = {},
                onLongClick = {}
            )
            MessageBubble(
                notification = previewNotifications[1],
                isSelectionMode = false,
                isSelected = false,
                onClick = {},
                onLongClick = {}
            )
        }
    }
}

/** 메시지 버블 프리뷰 - 단체 채팅 (발신자 이름 표시) */
@Preview(showBackground = true, name = "메시지 버블 - 단체 채팅")
@Composable
private fun MessageBubbleGroupChatPreview() {
    com.android.notikeep.presentation.ui.theme.NotiKeepTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            previewGroupNotifications.forEach { notification ->
                MessageBubble(
                    notification = notification,
                    isSelectionMode = false,
                    isSelected = false,
                    onClick = {},
                    onLongClick = {}
                )
            }
        }
    }
}

/** 메시지 버블 프리뷰 - 선택 모드 */
@Preview(showBackground = true, name = "메시지 버블 - 선택 모드")
@Composable
private fun MessageBubbleSelectionPreview() {
    com.android.notikeep.presentation.ui.theme.NotiKeepTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MessageBubble(
                notification = previewNotifications[0],
                isSelectionMode = true,
                isSelected = true,
                onClick = {},
                onLongClick = {}
            )
            MessageBubble(
                notification = previewNotifications[1],
                isSelectionMode = true,
                isSelected = false,
                onClick = {},
                onLongClick = {}
            )
        }
    }
}

/** 발신자 아이콘 프리뷰 - 이미지 없음 (이니셜 폴백) */
@Preview(showBackground = true, name = "발신자 아이콘 - 이니셜 폴백")
@Composable
private fun SenderIconInitialPreview() {
    com.android.notikeep.presentation.ui.theme.NotiKeepTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SenderIcon(senderIconPath = null, senderName = "홍길동")
            SenderIcon(senderIconPath = null, senderName = "김영희")
            SenderIcon(senderIconPath = null, senderName = "박민준")
            SenderIcon(senderIconPath = null, senderName = "A")
        }
    }
}

/** 발신자 아이콘 프리뷰 - 이미지 파일 없는 경로 (이니셜 폴백) */
@Preview(showBackground = true, name = "발신자 아이콘 - 잘못된 경로 폴백")
@Composable
private fun SenderIconInvalidPathPreview() {
    com.android.notikeep.presentation.ui.theme.NotiKeepTheme {
        Row(modifier = Modifier.padding(16.dp)) {
            SenderIcon(senderIconPath = "/non/existent/path.png", senderName = "이백엔드")
        }
    }
}
