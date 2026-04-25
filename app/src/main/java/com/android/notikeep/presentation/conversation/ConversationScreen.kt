package com.android.notikeep.presentation.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.activity.compose.BackHandler
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

// Stateful
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

// Stateless
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
    BackHandler(enabled = uiState.isSelectionMode) {
        onClearSelection()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.title) },
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
            reverseLayout = true
        ) {
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
                key = notifications.itemKey { it.id }
            ) { index ->
                val notification = notifications[index] ?: return@items
                MessageBubble(
                    notification = notification,
                    isSelectionMode = uiState.isSelectionMode,
                    isSelected = uiState.selectedNotificationIds.contains(notification.id),
                    onClick = {
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

@Composable
fun MessageBubble(
    notification: AppNotification,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
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
        // 프로필 아이콘 (단톡방 또는 프로필 이미지가 있을 때)
        if (isGroupChat || notification.senderIconPath != null) {
            SenderIcon(
                senderIconPath = notification.senderIconPath,
                senderName = notification.title
            )
        }
        Column(modifier = Modifier.widthIn(max = 260.dp)) {
            if (isGroupChat) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }
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
            Text(
                text = formatTime(notification.receivedAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

@Composable
fun SenderIcon(senderIconPath: String?, senderName: String) {
    val iconFile = senderIconPath?.let { File(it) }
    if (iconFile != null && iconFile.exists()) {
        AsyncImage(
            model = iconFile,
            contentDescription = senderName,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        // 이니셜 폴백
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

private fun formatTime(millis: Long): String =
    SimpleDateFormat("MM.dd HH:mm", Locale.getDefault()).format(Date(millis))

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
