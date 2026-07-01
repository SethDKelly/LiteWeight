package com.liteweight.progression.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liteweight.progression.data.ProgressionRepository
import com.liteweight.progression.domain.ProgressionSchemeDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProgressionSchemeDetailUiState(
    val detail: ProgressionSchemeDetail? = null,
)

@HiltViewModel
class ProgressionSchemeDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val progressionRepository: ProgressionRepository,
    ) : ViewModel() {
        private val schemeId: Long = savedStateHandle.get<Long>("schemeId") ?: 0L
        private val _uiState = MutableStateFlow(ProgressionSchemeDetailUiState())
        val uiState: StateFlow<ProgressionSchemeDetailUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                val detail = progressionRepository.getSchemeDetail(schemeId)
                _uiState.update { it.copy(detail = detail) }
            }
        }
    }
