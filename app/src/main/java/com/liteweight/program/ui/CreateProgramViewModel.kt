package com.liteweight.program.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liteweight.program.data.ProgramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class CreateProgramViewModel
    @Inject
    constructor(
        private val programRepository: ProgramRepository,
    ) : ViewModel() {
        fun createProgram(name: String, onCreated: (Long) -> Unit) {
            viewModelScope.launch {
                val trimmed = name.trim()
                if (trimmed.isEmpty()) return@launch
                val programId = programRepository.createCustomProgram(trimmed, splitType = null)
                programRepository.addDay(programId, "Day 1")
                onCreated(programId)
            }
        }
    }
