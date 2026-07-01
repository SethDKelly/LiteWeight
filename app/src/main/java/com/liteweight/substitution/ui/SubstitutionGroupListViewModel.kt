package com.liteweight.substitution.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liteweight.substitution.data.SubstitutionRepository
import com.liteweight.substitution.domain.SubstitutionGroupSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SubstitutionGroupListViewModel
    @Inject
    constructor(
        private val substitutionRepository: SubstitutionRepository,
    ) : ViewModel() {
        private val _groups = MutableStateFlow<List<SubstitutionGroupSummary>>(emptyList())
        val groups: StateFlow<List<SubstitutionGroupSummary>> = _groups.asStateFlow()

        init {
            viewModelScope.launch {
                substitutionRepository.observeGroups().collect { _groups.value = it }
            }
        }

        fun createGroup(name: String, onCreated: (Long) -> Unit) {
            viewModelScope.launch {
                val id = substitutionRepository.createGroup(name)
                onCreated(id)
            }
        }
    }
