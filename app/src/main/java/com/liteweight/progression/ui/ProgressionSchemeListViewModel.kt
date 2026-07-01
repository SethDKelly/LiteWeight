package com.liteweight.progression.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liteweight.progression.data.ProgressionRepository
import com.liteweight.progression.domain.ProgressionSchemeSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProgressionSchemeListUiState(
    val schemes: List<ProgressionSchemeSummary> = emptyList(),
)

@HiltViewModel
class ProgressionSchemeListViewModel
    @Inject
    constructor(
        private val progressionRepository: ProgressionRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ProgressionSchemeListUiState())
        val uiState: StateFlow<ProgressionSchemeListUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                progressionRepository.observeSchemes().collect { schemes ->
                    _uiState.update { it.copy(schemes = schemes) }
                }
            }
        }
    }
