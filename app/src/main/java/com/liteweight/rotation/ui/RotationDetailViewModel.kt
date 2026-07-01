package com.liteweight.rotation.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liteweight.program.data.ProgramRepository
import com.liteweight.program.domain.ProgramSummary
import com.liteweight.rotation.data.RotationRepository
import com.liteweight.rotation.domain.RotationPlanDetail
import com.liteweight.rotation.domain.RotationResolution
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RotationDetailUiState(
    val detail: RotationPlanDetail? = null,
    val resolution: RotationResolution? = null,
    val programs: List<ProgramSummary> = emptyList(),
)

@HiltViewModel
class RotationDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val rotationRepository: RotationRepository,
        private val programRepository: ProgramRepository,
    ) : ViewModel() {
        private val planId: Long = savedStateHandle.get<Long>("planId") ?: 0L
        private val _uiState = MutableStateFlow(RotationDetailUiState())
        val uiState: StateFlow<RotationDetailUiState> = _uiState.asStateFlow()

        init {
            refresh()
            viewModelScope.launch {
                programRepository.observePrograms().collect { programs ->
                    _uiState.update { it.copy(programs = programs) }
                }
            }
        }

        fun refresh() {
            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        detail = rotationRepository.getPlanDetail(planId),
                        resolution = rotationRepository.resolve(planId),
                    )
                }
            }
        }

        fun activate() {
            viewModelScope.launch {
                rotationRepository.activatePlan(planId)
                refresh()
            }
        }

        fun addProgram(programId: Long, label: String?) {
            viewModelScope.launch {
                rotationRepository.addSlot(planId, programId, label)
                refresh()
            }
        }
    }

@HiltViewModel
class CreateRotationViewModel
    @Inject
    constructor(
        private val rotationRepository: RotationRepository,
    ) : ViewModel() {
        fun create(
            name: String,
            cadenceType: com.liteweight.rotation.domain.CadenceType,
            cadenceInterval: Int,
            onCreated: (Long) -> Unit,
        ) {
            viewModelScope.launch {
                val id = rotationRepository.createPlan(name, cadenceType, cadenceInterval)
                onCreated(id)
            }
        }
    }
