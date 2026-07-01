package com.liteweight.session.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liteweight.session.domain.SessionSummary
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionHistoryScreen(
    onBack: () -> Unit,
    viewModel: SessionHistoryViewModel = hiltViewModel(),
) {
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout history") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            if (sessions.isEmpty()) {
                item { Text("No completed workouts yet.", modifier = Modifier.padding(16.dp)) }
            } else {
                items(sessions, key = { it.id }) { session ->
                    SessionHistoryRow(session = session, formatter = formatter)
                }
            }
        }
    }
}

@Composable
private fun SessionHistoryRow(
    session: SessionSummary,
    formatter: DateFormat,
) {
    val whenText =
        session.completedAtEpochMs?.let { formatter.format(Date(it)) }
            ?: formatter.format(Date(session.startedAtEpochMs))
    ListItem(
        headlineContent = { Text(whenText) },
        supportingContent = { Text("${session.exerciseCount} exercises") },
    )
}
