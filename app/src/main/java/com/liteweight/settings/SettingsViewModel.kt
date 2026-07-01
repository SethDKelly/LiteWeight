package com.liteweight.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liteweight.core.data.UserPreferencesRepository
import com.liteweight.exercise.domain.WeightUnit
import com.liteweight.program.domain.PrefillMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val userPreferencesRepository: UserPreferencesRepository,
    ) : ViewModel() {
        private val _weightUnit = MutableStateFlow(WeightUnit.KG)
        val weightUnit: StateFlow<WeightUnit> = _weightUnit.asStateFlow()

        private val _prefillMode = MutableStateFlow(PrefillMode.CARRY_LAST_SUCCESS)
        val prefillMode: StateFlow<PrefillMode> = _prefillMode.asStateFlow()

        init {
            viewModelScope.launch {
                _weightUnit.value = userPreferencesRepository.getWeightUnit()
                _prefillMode.value = userPreferencesRepository.getPrefillMode()
            }
        }

        fun setWeightUnit(unit: WeightUnit) {
            viewModelScope.launch {
                userPreferencesRepository.setWeightUnit(unit)
                _weightUnit.value = unit
            }
        }

        fun setPrefillMode(mode: PrefillMode) {
            viewModelScope.launch {
                userPreferencesRepository.setPrefillMode(mode)
                _prefillMode.value = mode
            }
        }
    }
