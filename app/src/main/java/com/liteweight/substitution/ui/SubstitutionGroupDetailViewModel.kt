package com.liteweight.substitution.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liteweight.exercise.data.ExerciseRepository
import com.liteweight.exercise.domain.ExerciseSummary
import com.liteweight.substitution.data.SubstitutionRepository
import com.liteweight.substitution.domain.SubstitutionGroupDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SubstitutionGroupDetailUiState(
    val detail: SubstitutionGroupDetail? = null,
    val allExercises: List<ExerciseSummary> = emptyList(),
)

@HiltViewModel
class SubstitutionGroupDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val substitutionRepository: SubstitutionRepository,
        private val exerciseRepository: ExerciseRepository,
    ) : ViewModel() {
        private val groupId: Long = savedStateHandle.get<Long>("groupId") ?: 0L
        private val _uiState = MutableStateFlow(SubstitutionGroupDetailUiState())
        val uiState: StateFlow<SubstitutionGroupDetailUiState> = _uiState.asStateFlow()

        init {
            refresh()
            viewModelScope.launch {
                exerciseRepository.observeAllExercises().collect { exercises ->
                    _uiState.update { it.copy(allExercises = exercises) }
                }
            }
        }

        fun refresh() {
            viewModelScope.launch {
                _uiState.update { it.copy(detail = substitutionRepository.getGroupDetail(groupId)) }
            }
        }

        fun addMember(exerciseKindId: Long) {
            viewModelScope.launch {
                substitutionRepository.addMember(groupId, exerciseKindId)
                refresh()
            }
        }
    }
