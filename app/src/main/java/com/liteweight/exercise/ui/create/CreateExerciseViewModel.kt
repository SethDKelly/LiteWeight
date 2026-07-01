package com.liteweight.exercise.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liteweight.exercise.data.ExerciseRepository
import com.liteweight.exercise.domain.BodyPosition
import com.liteweight.exercise.domain.CreateStructuredExerciseInput
import com.liteweight.exercise.domain.Equipment
import com.liteweight.exercise.domain.ExerciseDisplayNameBuilder
import com.liteweight.exercise.domain.GripOrientation
import com.liteweight.exercise.domain.GripWidth
import com.liteweight.exercise.domain.PrimaryMovement
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateExerciseUiState(
    val selectedTab: Int = 0,
    val equipment: Equipment = Equipment.BARBELL,
    val bodyPosition: BodyPosition = BodyPosition.NONE,
    val primaryMovement: PrimaryMovement = PrimaryMovement.BENCH_PRESS,
    val gripWidth: GripWidth = GripWidth.NONE,
    val gripOrientation: GripOrientation = GripOrientation.NONE,
    val freeformName: String = "",
    val namePreview: String = "",
) {
    val canSave: Boolean =
        if (selectedTab == 1) freeformName.trim().isNotEmpty() else namePreview.isNotBlank()
}

@HiltViewModel
class CreateExerciseViewModel
    @Inject
    constructor(
        private val exerciseRepository: ExerciseRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(CreateExerciseUiState())
        val uiState: StateFlow<CreateExerciseUiState> = _uiState.asStateFlow()

        init {
            refreshPreview()
        }

        fun selectTab(tab: Int) {
            _uiState.update { it.copy(selectedTab = tab) }
        }

        fun onEquipmentChange(value: Equipment) {
            _uiState.update { it.copy(equipment = value) }
            refreshPreview()
        }

        fun onBodyPositionChange(value: BodyPosition) {
            _uiState.update { it.copy(bodyPosition = value) }
            refreshPreview()
        }

        fun onMovementChange(value: PrimaryMovement) {
            _uiState.update { it.copy(primaryMovement = value) }
            refreshPreview()
        }

        fun onGripWidthChange(value: GripWidth) {
            _uiState.update { it.copy(gripWidth = value) }
            refreshPreview()
        }

        fun onGripOrientationChange(value: GripOrientation) {
            _uiState.update { it.copy(gripOrientation = value) }
            refreshPreview()
        }

        fun onFreeformNameChange(value: String) {
            _uiState.update { it.copy(freeformName = value) }
        }

        fun save(onCreated: (Long) -> Unit) {
            viewModelScope.launch {
                val state = _uiState.value
                val id =
                    if (state.selectedTab == 1) {
                        exerciseRepository.createFreeform(state.freeformName)
                    } else {
                        exerciseRepository.createStructured(
                            CreateStructuredExerciseInput(
                                equipment = state.equipment,
                                bodyPosition = state.bodyPosition,
                                primaryMovement = state.primaryMovement,
                                gripWidth = state.gripWidth,
                                gripOrientation = state.gripOrientation,
                            ),
                        )
                    }
                onCreated(id)
            }
        }

        private fun refreshPreview() {
            val state = _uiState.value
            val preview =
                ExerciseDisplayNameBuilder.build(
                    equipment = state.equipment,
                    bodyPosition = state.bodyPosition,
                    primaryMovement = state.primaryMovement,
                    primaryMovementFreeform = null,
                    gripWidth = state.gripWidth,
                    gripOrientation = state.gripOrientation,
                    customDisplayName = null,
                )
            _uiState.update { it.copy(namePreview = preview) }
        }
    }
