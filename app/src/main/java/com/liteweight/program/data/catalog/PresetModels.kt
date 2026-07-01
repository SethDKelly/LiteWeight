package com.liteweight.program.data.catalog

import kotlinx.serialization.Serializable

@Serializable
data class PresetProgramBundle(
    val version: Int,
    val presets: List<PresetProgramDto>,
)

@Serializable
data class PresetProgramDto(
    val presetId: String,
    val name: String,
    val difficulty: String? = null,
    val splitType: String? = null,
    val days: List<PresetDayDto>,
)

@Serializable
data class PresetDayDto(
    val name: String,
    val exercises: List<PresetExerciseDto>,
)

@Serializable
data class PresetExerciseDto(
    val catalogId: String,
    val targetSets: Int,
    val targetRepsMin: Int? = null,
    val targetRepsMax: Int? = null,
    val notes: String? = null,
)
