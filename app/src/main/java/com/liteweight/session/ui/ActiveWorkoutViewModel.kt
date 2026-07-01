package com.liteweight.session.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liteweight.core.data.UserPreferencesRepository
import com.liteweight.exercise.data.ExerciseRepository
import com.liteweight.exercise.domain.ExerciseFilters
import com.liteweight.exercise.domain.ExerciseSummary
import com.liteweight.substitution.data.SubstitutionRepository
import com.liteweight.progression.data.ProgressionRepository
import com.liteweight.progression.domain.ActiveProgressionContext
import com.liteweight.session.data.WorkoutRepository
import com.liteweight.session.data.local.SessionExerciseWithName
import com.liteweight.session.data.local.WorkoutSetEntryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActiveWorkoutUiState(
    val weightLabel: String = "kg",
    val exercises: List<SessionExerciseWithName> = emptyList(),
    val setsByEntry: Map<Long, List<WorkoutSetEntryEntity>> = emptyMap(),
    val allExercises: List<ExerciseSummary> = emptyList(),
    val swapSuggestions: List<ExerciseSummary> = emptyList(),
    val progression: ActiveProgressionContext? = null,
)

@HiltViewModel
class ActiveWorkoutViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val workoutRepository: WorkoutRepository,
        private val exerciseRepository: ExerciseRepository,
        private val substitutionRepository: SubstitutionRepository,
        private val progressionRepository: ProgressionRepository,
        private val userPreferencesRepository: UserPreferencesRepository,
    ) : ViewModel() {
        private val sessionId: Long = savedStateHandle.get<Long>("sessionId") ?: 0L
        private val _uiState = MutableStateFlow(ActiveWorkoutUiState())
        val uiState: StateFlow<ActiveWorkoutUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                val unit = userPreferencesRepository.getWeightUnit()
                _uiState.update {
                    it.copy(weightLabel = userPreferencesRepository.weightLabel(unit))
                }
            }
            viewModelScope.launch {
                _uiState.update {
                    it.copy(progression = progressionRepository.getActiveProgressionContext())
                }
            }
            viewModelScope.launch {
                exerciseRepository.observeExercises(ExerciseFilters()).collect { all ->
                    _uiState.update { it.copy(allExercises = all) }
                }
            }
            viewModelScope.launch {
                workoutRepository.observeSessionExercises(sessionId).collect { exercises ->
                    _uiState.update { it.copy(exercises = exercises) }
                    exercises.forEach { exercise ->
                        observeSets(exercise.entryId)
                    }
                }
            }
        }

        private fun observeSets(entryId: Long) {
            viewModelScope.launch {
                workoutRepository.observeSets(entryId).collect { sets ->
                    _uiState.update { state ->
                        state.copy(setsByEntry = state.setsByEntry + (entryId to sets))
                    }
                }
            }
        }

        fun prepareSwap(entryId: Long) {
            viewModelScope.launch {
                val exerciseKindId =
                    _uiState.value.exercises.firstOrNull { it.entryId == entryId }?.exerciseKindId
                        ?: return@launch
                val muscleSuggestions = exerciseRepository.suggestSubstitutes(exerciseKindId)
                val groupSuggestions = substitutionRepository.suggestFromGroups(exerciseKindId)
                val merged =
                    (groupSuggestions + muscleSuggestions)
                        .distinctBy { it.id }
                        .take(8)
                _uiState.update { it.copy(swapSuggestions = merged) }
            }
        }

        fun clearSwapSuggestions() {
            _uiState.update { it.copy(swapSuggestions = emptyList()) }
        }

        fun addExercise(exerciseKindId: Long) {
            viewModelScope.launch {
                workoutRepository.addExercise(sessionId, exerciseKindId)
            }
        }

        fun swapExercise(entryId: Long, newExerciseKindId: Long) {
            viewModelScope.launch {
                workoutRepository.swapExercise(entryId, newExerciseKindId)
                clearSwapSuggestions()
            }
        }

        fun logSet(entryId: Long, load: Double?, reps: Int?) {
            viewModelScope.launch {
                workoutRepository.addSet(entryId, load, reps)
            }
        }

        fun complete(onFinished: () -> Unit) {
            viewModelScope.launch {
                workoutRepository.completeSession(sessionId)
                onFinished()
            }
        }
    }
