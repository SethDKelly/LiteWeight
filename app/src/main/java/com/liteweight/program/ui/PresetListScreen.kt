package com.liteweight.program.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import com.liteweight.program.domain.PresetProgramSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetListScreen(
    onBack: () -> Unit,
    onInstalled: (Long) -> Unit,
    viewModel: PresetListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preset programs") },
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
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(uiState.presets, key = { it.presetId }) { preset ->
                PresetRow(
                    preset = preset,
                    isInstalling = uiState.installingId == preset.presetId,
                    onInstall = { viewModel.install(preset.presetId, onInstalled) },
                )
            }
        }
    }
}

@Composable
private fun PresetRow(
    preset: PresetProgramSummary,
    isInstalling: Boolean,
    onInstall: () -> Unit,
) {
    val subtitle =
        buildList {
            add("${preset.dayCount} days")
            preset.splitType?.let { add(it) }
            preset.difficulty?.let { add(it) }
        }.joinToString(" · ")

    ListItem(
        headlineContent = { Text(preset.name) },
        supportingContent = {
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        },
        trailingContent = {
            if (preset.isInstalled) {
                Text("Installed")
            } else {
                Button(onClick = onInstall, enabled = !isInstalling) {
                    Text(if (isInstalling) "Installing…" else "Install")
                }
            }
        },
    )
}
