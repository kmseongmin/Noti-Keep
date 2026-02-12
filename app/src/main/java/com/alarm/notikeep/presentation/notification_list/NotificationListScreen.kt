package com.alarm.notikeep.presentation.notification_list

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alarm.notikeep.domain.model.NotificationItem
import com.alarm.notikeep.util.DateTimeUtil

@Composable
fun NotificationListScreen(
    viewModel: NotificationListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    NotificationListContent(
        notifications = uiState.notifications,
        isLoading = uiState.isLoading
    )
}

@Composable
fun NotificationListContent(
    notifications: List<NotificationItem>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "NotiKeep",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    ) { paddingValues ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "저장된 알림이 없습니다",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notifications) { notification ->
                    NotificationItemCard(notification = notification)
                }
            }
        }
    }
}

@Composable
fun NotificationItemCard(
    notification: NotificationItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            notification.iconData?.let { iconBytes ->
                val bitmap = BitmapFactory.decodeByteArray(iconBytes, 0, iconBytes.size)
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "App Icon",
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.appName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                notification.title?.let { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                notification.content?.let { content ->
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = DateTimeUtil.formatTimestamp(notification.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationListContentPreview() {
    MaterialTheme {
        NotificationListContent(
            notifications = listOf(
                NotificationItem(
                    id = 1,
                    packageName = "com.example.app",
                    appName = "Example App",
                    title = "Test Notification",
                    content = "This is a test notification content",
                    timestamp = System.currentTimeMillis()
                ),
                NotificationItem(
                    id = 2,
                    packageName = "com.example.messenger",
                    appName = "Messenger",
                    title = "New Message",
                    content = "You have received a new message from John",
                    timestamp = System.currentTimeMillis() - 3600000
                )
            ),
            isLoading = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationListContentEmptyPreview() {
    MaterialTheme {
        NotificationListContent(
            notifications = emptyList(),
            isLoading = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationItemCardPreview() {
    MaterialTheme {
        NotificationItemCard(
            notification = NotificationItem(
                id = 1,
                packageName = "com.example.app",
                appName = "Example App",
                title = "Test Notification",
                content = "This is a test notification content with a longer text to see how it looks when it wraps to multiple lines",
                timestamp = System.currentTimeMillis()
            )
        )
    }
}
