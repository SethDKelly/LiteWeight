package com.liteweight.generator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liteweight.generator.data.RoutineGeneratorRepository
import com.liteweight.generator.domain.EquipmentProfile
import com.liteweight.generator.domain.GeneratorInput
import com.liteweight.generator.domain.ProgramDraft
import com.liteweight.generator.domain.TrainingGoal
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RoutineGeneratorUiState(
    val daysPerWeek: Int = 3,
    val goal: TrainingGoal = TrainingGoal.GENERAL,
    val equipment: EquipmentProfile = EquipmentProfile.FULL_GYM,
    val draft: ProgramDraft? = null,
)

@HiltViewModel
class RoutineGeneratorViewModel
    @Inject
    constructor(
        private val generatorRepository: RoutineGeneratorRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(RoutineGeneratorUiState())
        val uiState: StateFlow<RoutineGeneratorUiState> = _uiState.asStateFlow()

        fun setDaysPerWeek(days: Int) {
            _uiState.update { it.copy(daysPerWeek = days) }
        }

        fun setGoal(goal: TrainingGoal) {
            _uiState.update { it.copy(goal = goal) }
        }

        fun setEquipment(equipment: EquipmentProfile) {
            _uiState.update { it.copy(equipment = equipment) }
        }

        fun generate() {
            val state = _uiState.value
            val draft =
                generatorRepository.generateDraft(
                    GeneratorInput(state.daysPerWeek, state.goal, state.equipment),
                )
            _uiState.update { it.copy(draft = draft) }
        }

        fun acceptDraft(onSaved: (Long) -> Unit) {
            val draft = _uiState.value.draft ?: return
            viewModelScope.launch {
                val programId = generatorRepository.acceptDraft(draft)
                onSaved(programId)
            }
        }
    }
