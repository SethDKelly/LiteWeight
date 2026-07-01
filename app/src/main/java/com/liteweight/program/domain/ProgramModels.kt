package com.liteweight.program.domain

import com.liteweight.progression.domain.ActiveProgressionContext
import com.liteweight.rotation.domain.RotationResolution
enum class ProgramSourceType {
    CUSTOM,
    PRESET_INSTALLED,
}

data class ProgramSummary(
    val id: Long,
    val name: String,
    val sourceType: ProgramSourceType,
    val splitType: String?,
    val dayCount: Int,
    val isActive: Boolean,
)

data class ProgramDayDetail(
    val id: Long,
    val dayIndex: Int,
    val name: String,
    val exercises: List<ProgramExerciseDetail>,
)

data class ProgramExerciseDetail(
    val id: Long,
    val exerciseKindId: Long,
    val displayName: String,
    val movementSlotId: String?,
    val movementSlotLabel: String?,
    val targetSets: Int,
    val targetRepsMin: Int?,
    val targetRepsMax: Int?,
    val notes: String?,
)

data class ProgramDetail(
    val id: Long,
    val name: String,
    val sourceType: ProgramSourceType,
    val splitType: String?,
    val progressionSchemeId: Long?,
    val days: List<ProgramDayDetail>,
)

data class PresetProgramSummary(
    val presetId: String,
    val name: String,
    val difficulty: String?,
    val splitType: String?,
    val dayCount: Int,
    val isInstalled: Boolean,
)

enum class PrefillMode {
    PRESCRIPTION_ONLY,
    CARRY_LAST_SUCCESS,
    BLANK,
}

data class ActiveProgramContext(
    val programId: Long,
    val programName: String,
    val dayIndex: Int,
    val dayName: String,
    val exerciseCount: Int,
    val progression: ActiveProgressionContext? = null,
    val rotation: RotationResolution? = null,
)
