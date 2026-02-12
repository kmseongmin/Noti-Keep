package com.alarm.notikeep.presentation.notification_list

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.alarm.notikeep.domain.model.NotificationItem
import com.alarm.notikeep.presentation.permission.PermissionScreen
import com.alarm.notikeep.presentation.theme.SkyBlue
import com.alarm.notikeep.presentation.theme.SkyBlueDark
import com.alarm.notikeep.presentation.theme.SkyBlueLight
import com.alarm.notikeep.util.DateTimeUtil

@Composable
fun NotificationListScreen(
    viewModel: NotificationListViewModel = hiltViewModel(),
    onAppExit: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (!uiState.hasNotificationPermission) {
        PermissionScreen(
            onRequestPermission = { viewModel.requestPermission() },
            onAppExit = onAppExit
        )
    } else {
        NotificationListContent(
            notifications = uiState.notifications,
            isLoading = uiState.isLoading
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationListContent(
    notifications: List<NotificationItem>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "NotiKeep",
                            tint = Color.White
                        )
                        Text(
                            text = "NotiKeep",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SkyBlue
                )
            )
        },
        containerColor = SkyBlueLight.copy(alpha = 0.3f)
    ) { paddingValues ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "No notifications",
                        modifier = Modifier.size(64.dp),
                        tint = SkyBlue.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "저장된 알림이 없습니다",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SkyBlueDark.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SkyBlueLight),
                contentAlignment = Alignment.Center
            ) {
                notification.iconData?.let { iconBytes ->
                    val bitmap = BitmapFactory.decodeByteArray(iconBytes, 0, iconBytes.size)
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "App Icon",
                        modifier = Modifier.size(32.dp)
                    )
                } ?: Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Default Icon",
                    modifier = Modifier.size(24.dp),
                    tint = SkyBlue
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.appName,
                    style = MaterialTheme.typography.labelMedium,
                    color = SkyBlueDark,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                notification.title?.let { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                notification.content?.let { content ->
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black.copy(alpha = 0.6f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = DateTimeUtil.formatTimestamp(notification.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = SkyBlue,
                    fontWeight = FontWeight.Medium
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
                ),
                NotificationItem(
                    id = 3,
                    packageName = "com.example.email",
                    appName = "Email",
                    title = "Important Email",
                    content = "You have a new email from your manager regarding the project deadline",
                    timestamp = System.currentTimeMillis() - 7200000
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
