package com.liteweight.program.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liteweight.program.data.ProgramRepository
import com.liteweight.program.domain.PresetProgramSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PresetListUiState(
    val presets: List<PresetProgramSummary> = emptyList(),
    val installingId: String? = null,
)

@HiltViewModel
class PresetListViewModel
    @Inject
    constructor(
        private val programRepository: ProgramRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(PresetListUiState())
        val uiState: StateFlow<PresetListUiState> = _uiState.asStateFlow()

        init {
            refresh()
        }

        fun refresh() {
            viewModelScope.launch {
                val presets = programRepository.listPresets()
                _uiState.update { it.copy(presets = presets) }
            }
        }

        fun install(presetId: String, onInstalled: (Long) -> Unit) {
            viewModelScope.launch {
                _uiState.update { it.copy(installingId = presetId) }
                val programId = programRepository.installPreset(presetId)
                refresh()
                _uiState.update { it.copy(installingId = null) }
                onInstalled(programId)
            }
        }
    }
