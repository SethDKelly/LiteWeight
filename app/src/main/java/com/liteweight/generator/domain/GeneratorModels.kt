package com.liteweight.generator.domain

enum class TrainingGoal {
    STRENGTH,
    HYPERTROPHY,
    GENERAL,
}

enum class EquipmentProfile {
    FULL_GYM,
    HOME_DUMBBELLS,
    BODYWEIGHT,
}

data class GeneratorInput(
    val daysPerWeek: Int,
    val goal: TrainingGoal,
    val equipment: EquipmentProfile,
)

data class DraftExercise(
    val movementSlotId: String?,
    val catalogId: String,
    val targetSets: Int,
    val targetRepsMin: Int,
    val targetRepsMax: Int,
)

data class DraftDay(
    val name: String,
    val exercises: List<DraftExercise>,
)

data class ProgramDraft(
    val name: String,
    val splitType: String,
    val days: List<DraftDay>,
)
