package com.liteweight.session.ui

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liteweight.exercise.domain.ExerciseSummary
import com.liteweight.progression.domain.ActiveProgressionContext
import com.liteweight.session.data.local.SessionExerciseWithName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    onBack: () -> Unit,
    onFinished: () -> Unit,
    viewModel: ActiveWorkoutViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showPicker by remember { mutableStateOf(false) }
    var swapEntryId by remember { mutableStateOf<Long?>(null) }
    var logEntry by remember { mutableStateOf<SessionExerciseWithName?>(null) }

    if (showPicker || swapEntryId != null) {
        ExercisePickerDialog(
            exercises = uiState.allExercises,
            suggestions = if (swapEntryId != null) uiState.swapSuggestions else emptyList(),
            onDismiss = {
                showPicker = false
                swapEntryId = null
                viewModel.clearSwapSuggestions()
            },
            onPick = { exercise ->
                if (swapEntryId != null) {
                    viewModel.swapExercise(swapEntryId!!, exercise.id)
                    swapEntryId = null
                } else {
                    viewModel.addExercise(exercise.id)
                    showPicker = false
                }
            },
        )
    }

    logEntry?.let { entry ->
        LogSetDialog(
            weightLabel = uiState.weightLabel,
            onDismiss = { logEntry = null },
            onSave = { load, reps ->
                viewModel.logSet(entry.entryId, load, reps)
                logEntry = null
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Active workout") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.complete(onFinished) },
                        enabled = uiState.exercises.isNotEmpty(),
                    ) {
                        Text("Finish")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showPicker = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add exercise")
            }
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            uiState.progression?.let { progression ->
                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = progressionBannerText(progression),
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
            items(uiState.exercises, key = { it.entryId }) { exercise ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row {
                            Text(exercise.displayName, modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = {
                                    swapEntryId = exercise.entryId
                                    viewModel.prepareSwap(exercise.entryId)
                                },
                            ) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = "Swap exercise")
                            }
                        }
                        val sets = uiState.setsByEntry[exercise.entryId].orEmpty()
                        sets.forEach { set ->
                            Text(
                                "Set ${set.setIndex + 1}: ${set.loadValue ?: "-"} ${uiState.weightLabel} × ${set.repCount ?: "-"} reps",
                            )
                        }
                        TextButton(onClick = { logEntry = exercise }) {
                            Text("Log set")
                        }
                    }
                }
            }
            }
        }
    }
}

private fun progressionBannerText(context: ActiveProgressionContext): String {
    val sessions =
        context.sessionsRequired?.let { req ->
            " · ${context.sessionsAtLevel}/$req sessions"
        }.orEmpty()
    return "Phase: ${context.levelName}$sessions"
}

@Composable
private fun ExercisePickerDialog(
    exercises: List<ExerciseSummary>,
    suggestions: List<ExerciseSummary>,
    onDismiss: () -> Unit,
    onPick: (ExerciseSummary) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (suggestions.isNotEmpty()) "Swap exercise" else "Choose exercise") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (suggestions.isNotEmpty()) {
                    item {
                        Text(
                            "Suggested substitutes",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                    items(suggestions, key = { "suggestion-${it.id}" }) { exercise ->
                        TextButton(onClick = { onPick(exercise) }, modifier = Modifier.fillMaxWidth()) {
                            Text(exercise.displayName)
                        }
                    }
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            "All exercises",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                }
                items(exercises, key = { it.id }) { exercise ->
                    TextButton(onClick = { onPick(exercise) }, modifier = Modifier.fillMaxWidth()) {
                        Text(exercise.displayName)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun LogSetDialog(
    weightLabel: String,
    onDismiss: () -> Unit,
    onSave: (Double?, Int?) -> Unit,
) {
    var loadText by remember { mutableStateOf("") }
    var repsText by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log set") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = loadText,
                    onValueChange = { loadText = it },
                    label = { Text("Weight ($weightLabel)") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = repsText,
                    onValueChange = { repsText = it },
                    label = { Text("Reps") },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(loadText.toDoubleOrNull(), repsText.toIntOrNull())
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
