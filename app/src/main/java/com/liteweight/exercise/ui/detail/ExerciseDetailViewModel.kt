package com.liteweight.exercise.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liteweight.analytics.data.AnalyticsRepository
import com.liteweight.analytics.domain.ChartMetric
import com.liteweight.analytics.domain.ChartWindow
import com.liteweight.analytics.domain.ExerciseAnalyticsSnapshot
import com.liteweight.analytics.domain.availableMetrics
import com.liteweight.core.data.UserPreferencesRepository
import com.liteweight.exercise.data.ExerciseRepository
import com.liteweight.exercise.domain.ExerciseDetail
import com.liteweight.exercise.domain.UnitType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExerciseDetailUiState(
    val isLoading: Boolean = true,
    val detail: ExerciseDetail? = null,
    val weightLabel: String = "kg",
    val chartMetric: ChartMetric = ChartMetric.TOP_SET_LOAD,
    val chartWindow: ChartWindow = ChartWindow.WEEKS_12,
    val analytics: ExerciseAnalyticsSnapshot? = null,
)

@HiltViewModel
class ExerciseDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val exerciseRepository: ExerciseRepository,
        private val analyticsRepository: AnalyticsRepository,
        private val userPreferencesRepository: UserPreferencesRepository,
    ) : ViewModel() {
        private val exerciseId: Long = savedStateHandle.get<Long>("exerciseId") ?: 0L
        private val _uiState = MutableStateFlow(ExerciseDetailUiState())
        val uiState: StateFlow<ExerciseDetailUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                val unit = userPreferencesRepository.getWeightUnit()
                _uiState.update {
                    it.copy(weightLabel = userPreferencesRepository.weightLabel(unit))
                }
            }
            viewModelScope.launch {
                val detail = exerciseRepository.getDetail(exerciseId)
                val defaultMetric =
                    when (detail?.unitType) {
                        UnitType.BODYWEIGHT -> ChartMetric.TOP_SET_REPS
                        else -> ChartMetric.TOP_SET_LOAD
                    }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        detail = detail,
                        chartMetric = defaultMetric,
                    )
                }
                refreshAnalytics()
            }
        }

        fun setChartMetric(metric: ChartMetric) {
            _uiState.update { it.copy(chartMetric = metric) }
            refreshAnalytics()
        }

        fun setChartWindow(window: ChartWindow) {
            _uiState.update { it.copy(chartWindow = window) }
            refreshAnalytics()
        }

        private fun refreshAnalytics() {
            viewModelScope.launch {
                val state = _uiState.value
                val metric =
                    if (state.chartMetric in availableMetrics(state.detail?.unitType ?: UnitType.WEIGHT)) {
                        state.chartMetric
                    } else {
                        availableMetrics(state.detail?.unitType ?: UnitType.WEIGHT).first()
                    }
                val analytics =
                    analyticsRepository.getAnalyticsSnapshot(
                        exerciseKindId = exerciseId,
                        chartMetric = metric,
                        chartWindow = state.chartWindow,
                    )
                _uiState.update { it.copy(analytics = analytics, chartMetric = metric) }
            }
        }
    }
