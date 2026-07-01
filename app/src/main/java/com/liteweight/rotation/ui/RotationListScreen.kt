package com.liteweight.rotation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liteweight.rotation.domain.RotationPlanSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RotationListScreen(
    onBack: () -> Unit,
    onCreateRotation: () -> Unit,
    onOpenRotation: (Long) -> Unit,
    viewModel: RotationListViewModel = hiltViewModel(),
) {
    val plans by viewModel.plans.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rotation plans") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateRotation) {
                Icon(Icons.Default.Add, contentDescription = "Create rotation")
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(plans, key = { it.id }) { plan ->
                ListItem(
                    modifier = Modifier.clickable { onOpenRotation(plan.id) },
                    headlineContent = { Text(plan.name) },
                    supportingContent = {
                        val cadence = "${plan.cadenceInterval} ${plan.cadenceType.name.lowercase()} · ${plan.slotCount} routines"
                        Text(
                            cadence + if (plan.isActive) " · Active" else "",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                )
            }
        }
    }
}
