package com.liteweight.exercise.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liteweight.exercise.data.ExerciseRepository
import com.liteweight.exercise.data.local.MuscleFilterOption
import com.liteweight.exercise.domain.Equipment
import com.liteweight.exercise.domain.ExerciseFilters
import com.liteweight.exercise.domain.ExerciseSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExerciseListUiState(
    val query: String = "",
    val equipment: Equipment? = null,
    val mechanics: String? = null,
    val force: String? = null,
    val muscleSlug: String? = null,
    val targetMuscles: List<MuscleFilterOption> = emptyList(),
    val exercises: List<ExerciseSummary> = emptyList(),
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExerciseListViewModel
    @Inject
    constructor(
        private val exerciseRepository: ExerciseRepository,
    ) : ViewModel() {
        private val filtersFlow = MutableStateFlow(ExerciseFilters())
        private val _uiState = MutableStateFlow(ExerciseListUiState())
        val uiState: StateFlow<ExerciseListUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                exerciseRepository.observeTargetMuscles().collect { muscles ->
                    _uiState.update { it.copy(targetMuscles = muscles) }
                }
            }
            viewModelScope.launch {
                filtersFlow
                    .flatMapLatest { filters -> exerciseRepository.observeExercises(filters) }
                    .collect { exercises ->
                        _uiState.update { it.copy(exercises = exercises) }
                    }
            }
        }

        fun onQueryChange(value: String) {
            filtersFlow.update { it.copy(query = value) }
            _uiState.update { it.copy(query = value) }
        }

        fun toggleEquipment(equipment: Equipment) {
            val next = if (_uiState.value.equipment == equipment) null else equipment
            filtersFlow.update { it.copy(equipment = next) }
            _uiState.update { it.copy(equipment = next) }
        }

        fun toggleMechanics(mechanics: String) {
            val next = if (_uiState.value.mechanics == mechanics) null else mechanics
            filtersFlow.update { it.copy(mechanics = next) }
            _uiState.update { it.copy(mechanics = mechanics) }
        }

        fun toggleForce(force: String) {
            val next = if (_uiState.value.force == force) null else force
            filtersFlow.update { it.copy(force = next) }
            _uiState.update { it.copy(force = force) }
        }

        fun toggleMuscle(slug: String) {
            val next = if (_uiState.value.muscleSlug == slug) null else slug
            filtersFlow.update { it.copy(muscleSlug = next) }
            _uiState.update { it.copy(muscleSlug = next) }
        }

        fun clearFilters() {
            val query = _uiState.value.query
            filtersFlow.value = ExerciseFilters(query = query)
            _uiState.update {
                it.copy(
                    equipment = null,
                    mechanics = null,
                    force = null,
                    muscleSlug = null,
                )
            }
        }
    }
