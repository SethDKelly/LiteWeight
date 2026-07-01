package com.liteweight.session.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liteweight.session.data.WorkoutRepository
import com.liteweight.session.domain.SessionSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class SessionHistoryViewModel
    @Inject
    constructor(
        workoutRepository: WorkoutRepository,
    ) : ViewModel() {
        val sessions: StateFlow<List<SessionSummary>> =
            workoutRepository
                .observeCompletedSessions()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    }
