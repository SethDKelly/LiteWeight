package com.liteweight.program.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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
import com.liteweight.program.domain.ProgramSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramListScreen(
    onBack: () -> Unit,
    onCreateProgram: () -> Unit,
    onOpenPresets: () -> Unit,
    onOpenProgressionSchemes: () -> Unit,
    onOpenRotations: () -> Unit,
    onOpenGenerator: () -> Unit,
    onOpenSubstitutionGroups: () -> Unit,
    onOpenProgram: (Long) -> Unit,
    viewModel: ProgramListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Programs") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateProgram) {
                Icon(Icons.Default.Add, contentDescription = "Create program")
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                OutlinedButton(
                    onClick = onOpenPresets,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Browse preset programs")
                }
            }
            item {
                OutlinedButton(onClick = onOpenRotations, modifier = Modifier.fillMaxWidth()) {
                    Text("Rotation plans")
                }
            }
            item {
                OutlinedButton(onClick = onOpenGenerator, modifier = Modifier.fillMaxWidth()) {
                    Text("Generate routine")
                }
            }
            item {
                OutlinedButton(onClick = onOpenSubstitutionGroups, modifier = Modifier.fillMaxWidth()) {
                    Text("Substitution groups")
                }
            }
            item {
                OutlinedButton(
                    onClick = onOpenProgressionSchemes,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Progression schemes")
                }
            }
            items(uiState.programs, key = { it.id }) { program ->
                ProgramRow(program = program, onClick = { onOpenProgram(program.id) })
            }
        }
    }
}

@Composable
private fun ProgramRow(
    program: ProgramSummary,
    onClick: () -> Unit,
) {
    val subtitle =
        buildList {
            add("${program.dayCount} days")
            program.splitType?.let { add(it) }
            if (program.isActive) add("Active")
        }.joinToString(" · ")

    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(program.name) },
        supportingContent = {
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        },
    )
}
