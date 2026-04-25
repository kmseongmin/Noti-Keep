package com.android.notikeep.presentation.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.notikeep.domain.model.AppNotification

// Stateful
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(notifications = uiState.notifications)
}

// Stateless
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(notifications: List<AppNotification>) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("알림 목록") }) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(notifications, key = { it.id }) { notification ->
                NotificationListItem(notification = notification)
            }
        }
    }
}
