package com.liteweight.program.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liteweight.program.data.ProgramRepository
import com.liteweight.program.domain.ProgramSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProgramListUiState(
    val programs: List<ProgramSummary> = emptyList(),
)

@HiltViewModel
class ProgramListViewModel
    @Inject
    constructor(
        private val programRepository: ProgramRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ProgramListUiState())
        val uiState: StateFlow<ProgramListUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                programRepository.observePrograms().collect { programs ->
                    _uiState.update { it.copy(programs = programs) }
                }
            }
        }
    }
