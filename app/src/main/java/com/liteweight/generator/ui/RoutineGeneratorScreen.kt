package com.liteweight.generator.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liteweight.generator.domain.EquipmentProfile
import com.liteweight.generator.domain.TrainingGoal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineGeneratorScreen(
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
    viewModel: RoutineGeneratorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generate routine") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Days per week")
            listOf(3, 4, 5).forEach { days ->
                FilterChip(
                    selected = uiState.daysPerWeek == days,
                    onClick = { viewModel.setDaysPerWeek(days) },
                    label = { Text("$days days") },
                )
            }
            Text("Goal")
            TrainingGoal.entries.forEach { goal ->
                FilterChip(
                    selected = uiState.goal == goal,
                    onClick = { viewModel.setGoal(goal) },
                    label = { Text(goal.name.lowercase()) },
                )
            }
            Text("Equipment")
            EquipmentProfile.entries.forEach { equipment ->
                FilterChip(
                    selected = uiState.equipment == equipment,
                    onClick = { viewModel.setEquipment(equipment) },
                    label = { Text(equipment.name.lowercase().replace('_', ' ')) },
                )
            }
            Button(onClick = viewModel::generate, modifier = Modifier.fillMaxWidth()) {
                Text("Generate draft")
            }
            uiState.draft?.let { draft ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(draft.name)
                        draft.days.forEach { day ->
                            Text(day.name)
                            day.exercises.forEach { exercise ->
                                Text("• ${exercise.catalogId}: ${exercise.targetSets}×${exercise.targetRepsMin}–${exercise.targetRepsMax}")
                            }
                        }
                    }
                }
                Button(onClick = { viewModel.acceptDraft(onSaved) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Save as program")
                }
                OutlinedButton(onClick = viewModel::generate, modifier = Modifier.fillMaxWidth()) {
                    Text("Regenerate")
                }
            }
        }
    }
}
