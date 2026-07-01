package com.liteweight.rotation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RotationDetailScreen(
    onBack: () -> Unit,
    viewModel: RotationDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val detail = uiState.detail
    var showAddProgram by remember { mutableStateOf(false) }

    if (showAddProgram) {
        AddProgramToRotationDialog(
            programs = uiState.programs,
            onDismiss = { showAddProgram = false },
            onPick = { programId, label ->
                viewModel.addProgram(programId, label)
                showAddProgram = false
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detail?.name ?: "Rotation") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                uiState.resolution?.let { resolution ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Current: ${resolution.currentProgramName}")
                            resolution.currentLabel?.let { Text(it) }
                            Text("Next: ${resolution.nextProgramName}")
                            resolution.nextLabel?.let { Text(it) }
                            Text(
                                "Switches ${formatDate(resolution.periodEndsAtEpochMs)}",
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
            item {
                Button(onClick = viewModel::activate, modifier = Modifier.fillMaxWidth()) {
                    Text("Activate rotation")
                }
                TextButton(onClick = { showAddProgram = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Add program to rotation")
                }
            }
            if (detail != null) {
                items(detail.slots, key = { it.id }) { slot ->
                    Text("${slot.sortOrder + 1}. ${slot.programName}${slot.label?.let { " ($it)" } ?: ""}")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRotationScreen(
    onBack: () -> Unit,
    onCreated: (Long) -> Unit,
    viewModel: CreateRotationViewModel = hiltViewModel(),
) {
    var name by remember { mutableStateOf("") }
    var interval by remember { mutableStateOf("2") }
    var cadence by remember { mutableStateOf(com.liteweight.rotation.domain.CadenceType.WEEKS) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New rotation") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
            OutlinedTextField(value = interval, onValueChange = { interval = it }, label = { Text("Every N") }, singleLine = true)
            com.liteweight.rotation.domain.CadenceType.entries.forEach { type ->
                TextButton(onClick = { cadence = type }) {
                    Text(if (cadence == type) "✓ ${type.name}" else type.name)
                }
            }
            Button(
                onClick = {
                    viewModel.create(name, cadence, interval.toIntOrNull() ?: 2, onCreated)
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Create")
            }
        }
    }
}

@Composable
private fun AddProgramToRotationDialog(
    programs: List<com.liteweight.program.domain.ProgramSummary>,
    onDismiss: () -> Unit,
    onPick: (Long, String?) -> Unit,
) {
    var label by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add program") },
        text = {
            Column {
                OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Label (optional)") })
                LazyColumn {
                    items(programs, key = { it.id }) { program ->
                        TextButton(onClick = { onPick(program.id, label.ifBlank { null }) }) {
                            Text(program.name)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

private fun formatDate(epochMs: Long): String =
    SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(epochMs))
