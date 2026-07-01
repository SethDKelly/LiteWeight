package com.liteweight.exercise.domain

import com.liteweight.exercise.data.local.ExerciseKindEntity

data class ExerciseFilters(
    val query: String = "",
    val equipment: Equipment? = null,
    val mechanics: String? = null,
    val force: String? = null,
    val muscleSlug: String? = null,
)

data class ExerciseSummary(
    val id: Long,
    val displayName: String,
    val isBuiltin: Boolean,
    val unitType: UnitType,
    val mechanics: String? = null,
    val force: String? = null,
)

data class ExerciseDetail(
    val id: Long,
    val displayName: String,
    val isBuiltin: Boolean,
    val unitType: UnitType,
    val classification: String?,
    val instructions: List<String>,
    val comments: List<String>,
    val muscles: List<Pair<String, String>>,
)

data class CreateStructuredExerciseInput(
    val equipment: Equipment,
    val bodyPosition: BodyPosition,
    val primaryMovement: PrimaryMovement,
    val primaryMovementFreeform: String? = null,
    val gripWidth: GripWidth = GripWidth.NONE,
    val gripOrientation: GripOrientation = GripOrientation.NONE,
    val customDisplayName: String? = null,
    val unitType: UnitType = UnitType.WEIGHT,
)

fun ExerciseKindEntity.toSummary(mechanics: String? = null, force: String? = null) =
    ExerciseSummary(
        id = id,
        displayName = displayName,
        isBuiltin = isBuiltin,
        unitType = unitType,
        mechanics = mechanics,
        force = force,
    )
