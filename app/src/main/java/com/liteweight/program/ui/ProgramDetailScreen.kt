package com.liteweight.program.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.liteweight.program.domain.ProgramDayDetail
import com.liteweight.progression.domain.ActiveProgressionContext
import com.liteweight.progression.domain.AdvancementRuleType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramDetailScreen(
    onBack: () -> Unit,
    onOpenScheme: (Long) -> Unit,
    viewModel: ProgramDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val detail = uiState.detail
    var showAddDay by remember { mutableStateOf(false) }
    var addExerciseDay by remember { mutableStateOf<ProgramDayDetail?>(null) }
    var showSchemePicker by remember { mutableStateOf(false) }

    if (showAddDay) {
        NameDialog(
            title = "Add day",
            label = "Day name",
            onDismiss = { showAddDay = false },
            onConfirm = { name ->
                viewModel.addDay(name)
                showAddDay = false
            },
        )
    }

    if (showSchemePicker) {
        SchemePickerDialog(
            schemes = uiState.progressionSchemes,
            selectedSchemeId = detail?.progressionSchemeId,
            onDismiss = { showSchemePicker = false },
            onSelect = { schemeId ->
                viewModel.assignScheme(schemeId)
                showSchemePicker = false
            },
        )
    }

    addExerciseDay?.let { day ->
        AddExerciseDialog(
            exercises = uiState.allExercises,
            onDismiss = { addExerciseDay = null },
            onConfirm = { exercise, sets, repsMin, repsMax ->
                viewModel.addExercise(day.id, exercise.id, sets, repsMin, repsMax)
                addExerciseDay = null
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detail?.name ?: "Program") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDay = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add day")
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Button(
                    onClick = viewModel::activate,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = detail != null && !uiState.isActive,
                ) {
                    Text(if (uiState.isActive) "Currently active" else "Activate program")
                }
            }
            item {
                ProgressionCard(
                    assignedSchemeName = uiState.assignedSchemeName,
                    assignedSchemeId = detail?.progressionSchemeId,
                    activeProgression = uiState.activeProgression,
                    isActive = uiState.isActive,
                    onAssignScheme = { showSchemePicker = true },
                    onOpenScheme = onOpenScheme,
                    onManualAdvance = viewModel::manualAdvanceLevel,
                )
            }
            if (detail != null) {
                items(detail.days, key = { it.id }) { day ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(day.name)
                            day.exercises.forEach { exercise ->
                                val reps =
                                    when {
                                        exercise.targetRepsMin != null && exercise.targetRepsMax != null ->
                                            "${exercise.targetRepsMin}–${exercise.targetRepsMax} reps"
                                        exercise.targetRepsMin != null -> "${exercise.targetRepsMin}+ reps"
                                        else -> ""
                                    }
                                val slot = exercise.movementSlotLabel?.let { "[$it] " }.orEmpty()
                                Text("• $slot${exercise.displayName}: ${exercise.targetSets} sets $reps")
                            }
                            TextButton(onClick = { addExerciseDay = day }) {
                                Text("Add exercise")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressionCard(
    assignedSchemeName: String?,
    assignedSchemeId: Long?,
    activeProgression: ActiveProgressionContext?,
    isActive: Boolean,
    onAssignScheme: () -> Unit,
    onOpenScheme: (Long) -> Unit,
    onManualAdvance: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Progression", style = MaterialTheme.typography.titleMedium)
            Text(
                text = assignedSchemeName ?: "No scheme assigned",
                style = MaterialTheme.typography.bodyMedium,
            )
            OutlinedButton(onClick = onAssignScheme, modifier = Modifier.fillMaxWidth()) {
                Text(if (assignedSchemeName == null) "Assign progression scheme" else "Change scheme")
            }
            assignedSchemeId?.let { schemeId ->
                OutlinedButton(onClick = { onOpenScheme(schemeId) }, modifier = Modifier.fillMaxWidth()) {
                    Text("View levels")
                }
            }
            if (isActive && activeProgression != null) {
                Text(
                    text = progressionStatusLine(activeProgression),
                    style = MaterialTheme.typography.bodySmall,
                )
                if (activeProgression.advancementRule == AdvancementRuleType.MANUAL_ONLY) {
                    OutlinedButton(onClick = onManualAdvance, modifier = Modifier.fillMaxWidth()) {
                        Text("Advance to next level")
                    }
                }
            }
        }
    }
}

private fun progressionStatusLine(context: ActiveProgressionContext): String {
    val sessions =
        context.sessionsRequired?.let { req ->
            " · ${context.sessionsAtLevel}/$req sessions"
        }.orEmpty()
    return "${context.schemeName}: ${context.levelName}$sessions"
}

@Composable
private fun SchemePickerDialog(
    schemes: List<com.liteweight.progression.domain.ProgressionSchemeSummary>,
    selectedSchemeId: Long?,
    onDismiss: () -> Unit,
    onSelect: (Long?) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Progression scheme") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                item {
                    TextButton(onClick = { onSelect(null) }, modifier = Modifier.fillMaxWidth()) {
                        Text("None")
                    }
                }
                items(schemes, key = { it.id }) { scheme ->
                    TextButton(onClick = { onSelect(scheme.id) }, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "${scheme.name} (${scheme.levelCount} levels)" +
                                if (scheme.id == selectedSchemeId) " ✓" else "",
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun NameDialog(
    title: String,
    label: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(label) },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }, enabled = text.isNotBlank()) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun AddExerciseDialog(
    exercises: List<ExerciseSummary>,
    onDismiss: () -> Unit,
    onConfirm: (ExerciseSummary, Int, Int?, Int?) -> Unit,
) {
    var selected by remember { mutableStateOf<ExerciseSummary?>(null) }
    var setsText by remember { mutableStateOf("3") }
    var repsMinText by remember { mutableStateOf("8") }
    var repsMaxText by remember { mutableStateOf("12") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add exercise") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (selected == null) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(exercises, key = { it.id }) { exercise ->
                            TextButton(
                                onClick = { selected = exercise },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(exercise.displayName)
                            }
                        }
                    }
                } else {
                    Text(selected!!.displayName)
                    OutlinedTextField(
                        value = setsText,
                        onValueChange = { setsText = it },
                        label = { Text("Sets") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = repsMinText,
                        onValueChange = { repsMinText = it },
                        label = { Text("Reps min") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = repsMaxText,
                        onValueChange = { repsMaxText = it },
                        label = { Text("Reps max") },
                        singleLine = true,
                    )
                }
            }
        },
        confirmButton = {
            if (selected != null) {
                TextButton(
                    onClick = {
                        onConfirm(
                            selected!!,
                            setsText.toIntOrNull() ?: 3,
                            repsMinText.toIntOrNull(),
                            repsMaxText.toIntOrNull(),
                        )
                    },
                ) {
                    Text("Add")
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
