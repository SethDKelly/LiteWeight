package com.liteweight.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liteweight.exercise.domain.WeightUnit
import com.liteweight.program.domain.PrefillMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val weightUnit by viewModel.weightUnit.collectAsStateWithLifecycle()
    val prefillMode by viewModel.prefillMode.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Weight unit")
            SingleChoiceSegmentedButtonRow {
                WeightUnit.entries.forEachIndexed { index, unit ->
                    SegmentedButton(
                        selected = weightUnit == unit,
                        onClick = { viewModel.setWeightUnit(unit) },
                        shape = SegmentedButtonDefaults.itemShape(index, WeightUnit.entries.size),
                    ) {
                        Text(unit.name)
                    }
                }
            }

            Text("Workout prefill")
            Text(
                "When starting a program workout, how sets are prefilled.",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            )
            SingleChoiceSegmentedButtonRow {
                PrefillMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = prefillMode == mode,
                        onClick = { viewModel.setPrefillMode(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index, PrefillMode.entries.size),
                    ) {
                        Text(mode.label)
                    }
                }
            }
        }
    }
}

private val PrefillMode.label: String
    get() =
        when (this) {
            PrefillMode.PRESCRIPTION_ONLY -> "Rx only"
            PrefillMode.CARRY_LAST_SUCCESS -> "Last load"
            PrefillMode.BLANK -> "Blank"
        }
