package com.liteweight.progression.domain

enum class AdvancementRuleType {
    REP_TARGET,
    SESSION_COUNT,
    MANUAL_ONLY,
}

data class ProgressionLevel(
    val id: Long,
    val levelIndex: Int,
    val name: String,
    val loadIncrement: Double,
    val targetRepsMin: Int?,
    val targetRepsMax: Int?,
    val sessionsRequired: Int?,
    val advancementRule: AdvancementRuleType,
    val isDeload: Boolean,
)

data class ProgressionSchemeSummary(
    val id: Long,
    val name: String,
    val levelCount: Int,
)

data class ProgressionSchemeDetail(
    val id: Long,
    val name: String,
    val levels: List<ProgressionLevel>,
)

data class ActiveProgressionContext(
    val schemeId: Long,
    val schemeName: String,
    val levelIndex: Int,
    val levelName: String,
    val isDeload: Boolean,
    val advancementRule: AdvancementRuleType,
    val sessionsAtLevel: Int,
    val sessionsRequired: Int?,
    val loadIncrement: Double,
    val targetRepsMin: Int?,
    val targetRepsMax: Int?,
)

data class SetPerformance(
    val exerciseKindId: Long,
    val substitutedFromExerciseKindId: Long?,
    val repCount: Int?,
    val isWarmup: Boolean,
    val isCompleted: Boolean,
)

data class ExercisePrescription(
    val exerciseKindId: Long,
    val targetSets: Int,
    val targetRepsMin: Int?,
)
