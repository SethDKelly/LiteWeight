package com.liteweight.program.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liteweight.exercise.data.ExerciseRepository
import com.liteweight.exercise.domain.ExerciseSummary
import com.liteweight.program.data.ProgramRepository
import com.liteweight.program.domain.ProgramDetail
import com.liteweight.progression.data.ProgressionRepository
import com.liteweight.progression.domain.ActiveProgressionContext
import com.liteweight.progression.domain.ProgressionSchemeSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProgramDetailUiState(
    val detail: ProgramDetail? = null,
    val allExercises: List<ExerciseSummary> = emptyList(),
    val progressionSchemes: List<ProgressionSchemeSummary> = emptyList(),
    val assignedSchemeName: String? = null,
    val activeProgression: ActiveProgressionContext? = null,
    val isActive: Boolean = false,
)

@HiltViewModel
class ProgramDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val programRepository: ProgramRepository,
        private val exerciseRepository: ExerciseRepository,
        private val progressionRepository: ProgressionRepository,
    ) : ViewModel() {
        private val programId: Long = savedStateHandle.get<Long>("programId") ?: 0L
        private val _uiState = MutableStateFlow(ProgramDetailUiState())
        val uiState: StateFlow<ProgramDetailUiState> = _uiState.asStateFlow()

        init {
            refresh()
            viewModelScope.launch {
                exerciseRepository.observeAllExercises().collect { exercises ->
                    _uiState.update { it.copy(allExercises = exercises) }
                }
            }
            viewModelScope.launch {
                progressionRepository.observeSchemes().collect { schemes ->
                    _uiState.update { it.copy(progressionSchemes = schemes) }
                    refreshSchemeName()
                }
            }
            viewModelScope.launch {
                programRepository.observeActiveProgram().collect { active ->
                    val isActive = active?.programId == programId
                    _uiState.update {
                        it.copy(
                            isActive = isActive,
                            activeProgression =
                                if (isActive) {
                                    progressionRepository.getActiveProgressionContext()
                                } else {
                                    null
                                },
                        )
                    }
                }
            }
        }

        fun refresh() {
            viewModelScope.launch {
                val detail = programRepository.getProgramDetail(programId)
                _uiState.update { it.copy(detail = detail) }
                refreshSchemeName()
            }
        }

        private suspend fun refreshSchemeName() {
            val schemeId = _uiState.value.detail?.progressionSchemeId ?: return
            val name = progressionRepository.getSchemeDetail(schemeId)?.name
            _uiState.update { it.copy(assignedSchemeName = name) }
        }

        fun activate() {
            viewModelScope.launch {
                programRepository.activateProgram(programId)
                refresh()
            }
        }

        fun assignScheme(schemeId: Long?) {
            viewModelScope.launch {
                progressionRepository.assignSchemeToProgram(programId, schemeId)
                refresh()
            }
        }

        fun manualAdvanceLevel() {
            viewModelScope.launch {
                progressionRepository.manualAdvanceLevel()
                _uiState.update {
                    it.copy(activeProgression = progressionRepository.getActiveProgressionContext())
                }
            }
        }

        fun addDay(name: String) {
            viewModelScope.launch {
                if (name.isBlank()) return@launch
                programRepository.addDay(programId, name.trim())
                refresh()
            }
        }

        fun addExercise(
            programDayId: Long,
            exerciseKindId: Long,
            targetSets: Int,
            targetRepsMin: Int?,
            targetRepsMax: Int?,
        ) {
            viewModelScope.launch {
                programRepository.addExerciseToDay(
                    programDayId = programDayId,
                    exerciseKindId = exerciseKindId,
                    targetSets = targetSets,
                    targetRepsMin = targetRepsMin,
                    targetRepsMax = targetRepsMax,
                )
                refresh()
            }
        }
    }
