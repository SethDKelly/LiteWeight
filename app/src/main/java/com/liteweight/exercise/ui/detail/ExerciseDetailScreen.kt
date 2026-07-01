package com.liteweight.exercise.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liteweight.analytics.ui.ExerciseAnalyticsSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    onBack: () -> Unit,
    viewModel: ExerciseDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.detail?.displayName ?: "Exercise") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            uiState.isLoading ->
                CircularProgressIndicator(
                    modifier = Modifier.padding(innerPadding).padding(24.dp),
                )
            uiState.detail == null ->
                Text(
                    "Exercise not found",
                    modifier = Modifier.padding(innerPadding).padding(16.dp),
                )
            else -> {
                val detail = uiState.detail!!
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ExerciseAnalyticsSection(
                        analytics = uiState.analytics,
                        chartMetric = uiState.chartMetric,
                        chartWindow = uiState.chartWindow,
                        weightLabel = uiState.weightLabel,
                        onMetricChange = viewModel::setChartMetric,
                        onWindowChange = viewModel::setChartWindow,
                    )
                    detail.classification?.let {
                        Text("Classification", style = MaterialTheme.typography.titleMedium)
                        Text(it)
                    }
                    if (detail.muscles.isNotEmpty()) {
                        Text("Muscles", style = MaterialTheme.typography.titleMedium)
                        detail.muscles.forEach { (name, role) ->
                            Text("• $name ($role)")
                        }
                    }
                    if (detail.instructions.isNotEmpty()) {
                        Text("Instructions", style = MaterialTheme.typography.titleMedium)
                        detail.instructions.forEachIndexed { index, step ->
                            Text("${index + 1}. $step")
                        }
                    }
                    if (detail.comments.isNotEmpty()) {
                        Text("Comments", style = MaterialTheme.typography.titleMedium)
                        detail.comments.forEach { comment ->
                            Text("• $comment")
                        }
                    }
                }
            }
        }
    }
}
