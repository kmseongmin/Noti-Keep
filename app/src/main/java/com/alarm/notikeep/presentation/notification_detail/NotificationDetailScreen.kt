package com.alarm.notikeep.presentation.notification_detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.alarm.notikeep.domain.notification.NotificationClassifier
import com.alarm.notikeep.presentation.notification_list.NotificationItemCard
import com.alarm.notikeep.presentation.theme.NotiKeepDimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailScreen(
    packageName: String,
    threadKey: String,
    title: String?,
    onBack: () -> Unit,
    viewModel: NotificationDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val detailItems = remember(uiState.notifications, threadKey) {
        uiState.notifications
            .filter { NotificationClassifier.threadKey(it) == threadKey }
            .sortedByDescending { it.timestamp }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "${title ?: packageName} (${detailItems.size})")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (detailItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (uiState.isLoading) "알림을 불러오는 중입니다" else "표시할 알림이 없습니다",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(
                        horizontal = NotiKeepDimens.Space20,
                        vertical = NotiKeepDimens.Space16
                    ),
                verticalArrangement = Arrangement.spacedBy(NotiKeepDimens.Space16)
            ) {
                items(detailItems) { notification ->
                    NotificationItemCard(
                        notification = notification,
                        isRead = true,
                        showFullContent = true
                    )
                }
            }
        }
    }
}
