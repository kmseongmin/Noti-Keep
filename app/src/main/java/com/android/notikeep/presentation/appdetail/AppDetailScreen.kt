package com.android.notikeep.presentation.appdetail

import android.app.Notification
import androidx.compose.foundation.clickable
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
    AppDetailContent(uiState = uiState, onBack = onBack, onConversationClick = onConversationClick)
}

// Stateless
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailContent(
    uiState: AppDetailUiState,
    onBack: () -> Unit,
    onConversationClick: (packageName: String, conversationKey: String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.appName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
            items(uiState.conversations, key = { it.conversationKey }) { conversation ->
                val isMessaging = conversation.latest.category == Notification.CATEGORY_MESSAGE
                ConversationGroupItem(
                    conversation = conversation,
                    isMessaging = isMessaging,
                    onClick = if (isMessaging) {
                        {
                            onConversationClick(
                                conversation.latest.packageName,
                                conversation.conversationKey
                            )
                        }
                    } else null
                )
            }
        }
    }
}

@Composable
fun ConversationGroupItem(
    conversation: ConversationGroup,
    isMessaging: Boolean,
    onClick: (() -> Unit)?
) {
    val modifier = Modifier
        .fillMaxWidth()
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
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
                Text(
                    text = conversation.conversationKey,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (conversation.count > 1) {
                    Badge { Text(conversation.count.toString()) }
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
