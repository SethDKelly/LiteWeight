package com.liteweight.exercise.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liteweight.exercise.domain.Equipment
import com.liteweight.exercise.domain.ExerciseSummary

private val mechanicsOptions = listOf("compound", "isolated")
private val forceOptions = listOf("push", "pull", "legs")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseListScreen(
    onBack: () -> Unit,
    onCreateExercise: () -> Unit,
    onOpenExercise: (Long) -> Unit,
    viewModel: ExerciseListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val hasFilters =
        uiState.equipment != null ||
            uiState.mechanics != null ||
            uiState.force != null ||
            uiState.muscleSlug != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exercises") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (hasFilters) {
                        TextButton(onClick = viewModel::clearFilters) {
                            Text("Clear filters")
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateExercise) {
                Icon(Icons.Default.Add, contentDescription = "Create exercise")
            }
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
        ) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search exercises") },
                singleLine = true,
            )
            FilterChipRow(label = "Equipment") {
                Equipment.entries.forEach { equipment ->
                    FilterChip(
                        selected = uiState.equipment == equipment,
                        onClick = { viewModel.toggleEquipment(equipment) },
                        label = { Text(equipment.name.lowercase().replace('_', ' ')) },
                    )
                }
            }
            FilterChipRow(label = "Mechanics") {
                mechanicsOptions.forEach { mechanics ->
                    FilterChip(
                        selected = uiState.mechanics == mechanics,
                        onClick = { viewModel.toggleMechanics(mechanics) },
                        label = { Text(mechanics.replaceFirstChar { it.uppercase() }) },
                    )
                }
            }
            FilterChipRow(label = "Force") {
                forceOptions.forEach { force ->
                    FilterChip(
                        selected = uiState.force == force,
                        onClick = { viewModel.toggleForce(force) },
                        label = { Text(force.replaceFirstChar { it.uppercase() }) },
                    )
                }
            }
            if (uiState.targetMuscles.isNotEmpty()) {
                FilterChipRow(label = "Muscle") {
                    uiState.targetMuscles.forEach { muscle ->
                        FilterChip(
                            selected = uiState.muscleSlug == muscle.muscleSlug,
                            onClick = { viewModel.toggleMuscle(muscle.muscleSlug) },
                            label = { Text(muscle.displayName) },
                        )
                    }
                }
            }
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(uiState.exercises, key = { it.id }) { exercise ->
                    ExerciseRow(exercise = exercise, onClick = { onOpenExercise(exercise.id) })
                }
            }
        }
    }
}

@Composable
private fun FilterChipRow(
    label: String,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        androidx.compose.foundation.layout.Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun ExerciseRow(
    exercise: ExerciseSummary,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(exercise.displayName) },
        supportingContent = {
            Text(
                text = if (exercise.isBuiltin) "Built-in · ${exercise.unitType.name.lowercase()}" else "Custom",
                style = MaterialTheme.typography.bodySmall,
            )
        },
    )
}
