package com.liteweight.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liteweight.analytics.data.AnalyticsRepository
import com.liteweight.core.startup.AppInitializer
import com.liteweight.exercise.data.local.ExerciseKindDao
import com.liteweight.program.data.ProgramRepository
import com.liteweight.program.domain.ActiveProgramContext
import com.liteweight.session.data.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val exerciseCount: Int = 0,
    val activeProgram: ActiveProgramContext? = null,
    val sessionsThisWeek: Int = 0,
    val isLoading: Boolean = true,
)

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val appInitializer: AppInitializer,
        private val exerciseKindDao: ExerciseKindDao,
        private val workoutRepository: WorkoutRepository,
        private val programRepository: ProgramRepository,
        private val analyticsRepository: AnalyticsRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(HomeUiState())
        val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                appInitializer.initialize()
                val weekAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
                _uiState.update {
                    it.copy(
                        exerciseCount = exerciseKindDao.count(),
                        activeProgram = programRepository.getActiveContext(),
                        sessionsThisWeek = analyticsRepository.countCompletedSessionsSince(weekAgo),
                        isLoading = false,
                    )
                }
            }
            viewModelScope.launch {
                programRepository.observeActiveProgram().collect {
                    _uiState.update { state ->
                        state.copy(activeProgram = programRepository.getActiveContext())
                    }
                }
            }
        }

        fun startWorkout(onStarted: (Long) -> Unit) {
            viewModelScope.launch {
                val sessionId = workoutRepository.startOrResumeDraft()
                onStarted(sessionId)
            }
        }

        fun startEmptyWorkout(onStarted: (Long) -> Unit) {
            viewModelScope.launch {
                val sessionId = workoutRepository.startFreshWorkout()
                onStarted(sessionId)
            }
        }
    }
