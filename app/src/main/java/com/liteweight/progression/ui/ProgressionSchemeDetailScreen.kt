package com.liteweight.progression.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
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
import com.liteweight.progression.domain.AdvancementRuleType
import com.liteweight.progression.domain.ProgressionLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressionSchemeDetailScreen(
    onBack: () -> Unit,
    viewModel: ProgressionSchemeDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val detail = uiState.detail

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detail?.name ?: "Scheme") },
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
            if (detail != null) {
                items(detail.levels, key = { it.id }) { level ->
                    LevelCard(level = level)
                }
            }
        }
    }
}

@Composable
private fun LevelCard(level: ProgressionLevel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("${level.levelIndex + 1}. ${level.name}", style = MaterialTheme.typography.titleMedium)
            Text("Rule: ${level.advancementRule.label}", style = MaterialTheme.typography.bodySmall)
            level.targetRepsMin?.let { min ->
                val repRange =
                    if (level.targetRepsMax != null) "$min–${level.targetRepsMax} reps" else "$min+ reps"
                Text("Target: $repRange", style = MaterialTheme.typography.bodySmall)
            }
            if (level.loadIncrement != 0.0) {
                Text("Load step: ${level.loadIncrement}", style = MaterialTheme.typography.bodySmall)
            }
            if (level.isDeload) {
                Text("Deload phase (90% of last load)", style = MaterialTheme.typography.bodySmall)
            }
            level.sessionsRequired?.let { required ->
                Text("Advance after $required sessions", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private val AdvancementRuleType.label: String
    get() =
        when (this) {
            AdvancementRuleType.REP_TARGET -> "Hit rep targets"
            AdvancementRuleType.SESSION_COUNT -> "Session count"
            AdvancementRuleType.MANUAL_ONLY -> "Manual advance"
        }
