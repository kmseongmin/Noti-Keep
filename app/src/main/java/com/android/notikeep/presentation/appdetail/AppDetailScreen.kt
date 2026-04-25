package com.android.notikeep.presentation.appdetail

import android.app.Notification
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

// Stateful
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

// Stateless
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
                                onConversationClick(
                                    conversation.latest.packageName,
                                    conversation.conversationKey
                                )
                            }
                        }
                    } else if (uiState.isSelectionMode) {
                        { onItemClick(conversation.conversationKey) }
                    } else null,
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
                Text(
                    text = conversation.conversationKey,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (conversation.unreadCount > 0) {
                    Badge { Text(conversation.unreadCount.toString()) }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTime(conversation.latest.receivedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                if (isMessaging) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
        // 단톡방이면 보낸 사람 이름도 표시
        if (conversation.latest.subText?.isNotBlank() == true) {
            Text(
                text = conversation.latest.title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Text(
            text = conversation.latest.content,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

private fun formatTime(millis: Long): String =
    SimpleDateFormat("MM.dd HH:mm", Locale.getDefault()).format(Date(millis))

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
