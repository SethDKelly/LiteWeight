package com.liteweight.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenExercises: () -> Unit,
    onOpenPrograms: () -> Unit,
    onStartWorkout: (Long) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("LiteWeight") }) },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Train simple. Log fast.",
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = "${uiState.exerciseCount} exercises · ${uiState.sessionsThisWeek} sessions this week",
                style = MaterialTheme.typography.bodyLarge,
            )

            uiState.activeProgram?.let { active ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Active program", style = MaterialTheme.typography.labelLarge)
                        Text(active.programName, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${active.dayName} · ${active.exerciseCount} exercises",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        active.progression?.let { progression ->
                            Text(
                                "${progression.schemeName}: ${progression.levelName}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        active.rotation?.let { rotation ->
                            Text(
                                "Rotation: ${rotation.currentProgramName} → next ${rotation.nextProgramName}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = { viewModel.startWorkout(onStartWorkout) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (uiState.activeProgram != null) "Start program workout" else "Start workout")
                }
                OutlinedButton(
                    onClick = { viewModel.startEmptyWorkout(onStartWorkout) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Start empty workout")
                }
                OutlinedButton(
                    onClick = onOpenPrograms,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Programs")
                }
                OutlinedButton(
                    onClick = onOpenExercises,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Browse exercises")
                }
                OutlinedButton(
                    onClick = onOpenHistory,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Workout history")
                }
                OutlinedButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Settings")
                }
            }
        }
    }
}
