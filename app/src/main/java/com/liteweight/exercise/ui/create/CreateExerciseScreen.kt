package com.liteweight.exercise.ui.create

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liteweight.exercise.domain.BodyPosition
import com.liteweight.exercise.domain.Equipment
import com.liteweight.exercise.domain.GripOrientation
import com.liteweight.exercise.domain.GripWidth
import com.liteweight.exercise.domain.PrimaryMovement
import com.liteweight.exercise.ui.components.EnumDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExerciseScreen(
    onBack: () -> Unit,
    onCreated: (Long) -> Unit,
    viewModel: CreateExerciseViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create exercise") },
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
        ) {
            TabRow(selectedTabIndex = uiState.selectedTab) {
                Tab(selected = uiState.selectedTab == 0, onClick = { viewModel.selectTab(0) }, text = { Text("Structured") })
                Tab(selected = uiState.selectedTab == 1, onClick = { viewModel.selectTab(1) }, text = { Text("Free-form") })
            }
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (uiState.selectedTab == 0) {
                    EnumDropdown("Equipment", Equipment.entries, uiState.equipment, viewModel::onEquipmentChange)
                    EnumDropdown("Position", BodyPosition.entries, uiState.bodyPosition, viewModel::onBodyPositionChange)
                    EnumDropdown("Movement", PrimaryMovement.entries, uiState.primaryMovement, viewModel::onMovementChange)
                    EnumDropdown("Grip width", GripWidth.entries, uiState.gripWidth, viewModel::onGripWidthChange)
                    EnumDropdown("Grip orientation", GripOrientation.entries, uiState.gripOrientation, viewModel::onGripOrientationChange)
                    Text(
                        text = "Preview: ${uiState.namePreview}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                } else {
                    OutlinedTextField(
                        value = uiState.freeformName,
                        onValueChange = viewModel::onFreeformNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Exercise name") },
                        singleLine = true,
                    )
                }
            }
            Button(
                onClick = { viewModel.save(onCreated) },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.canSave,
            ) {
                Text("Save exercise")
            }
        }
    }
}
