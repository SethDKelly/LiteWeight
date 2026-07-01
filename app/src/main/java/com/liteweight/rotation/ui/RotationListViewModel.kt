package com.liteweight.rotation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liteweight.rotation.data.RotationRepository
import com.liteweight.rotation.domain.RotationPlanSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RotationListViewModel
    @Inject
    constructor(
        private val rotationRepository: RotationRepository,
    ) : ViewModel() {
        private val _plans = MutableStateFlow<List<RotationPlanSummary>>(emptyList())
        val plans: StateFlow<List<RotationPlanSummary>> = _plans.asStateFlow()

        init {
            viewModelScope.launch {
                rotationRepository.observePlans().collect { _plans.value = it }
            }
        }
    }
