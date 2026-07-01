package com.liteweight.exercise.domain

object ExerciseDisplayNameBuilder {
    fun build(
        equipment: Equipment?,
        bodyPosition: BodyPosition?,
        primaryMovement: PrimaryMovement?,
        primaryMovementFreeform: String?,
        gripWidth: GripWidth?,
        gripOrientation: GripOrientation?,
        customDisplayName: String?,
    ): String {
        customDisplayName?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }

        val parts = buildList {
            equipment?.takeUnless { it == Equipment.OTHER }?.let { add(it.label()) }
            bodyPosition?.takeUnless { it == BodyPosition.NONE }?.let { add(it.label()) }
            when (primaryMovement) {
                PrimaryMovement.OTHER -> primaryMovementFreeform?.trim()?.takeIf { it.isNotEmpty() }?.let { add(it) }
                null -> Unit
                else -> add(primaryMovement.label())
            }
        }

        val grip = buildGripLabel(gripWidth, gripOrientation)
        val base = parts.joinToString(" ").ifBlank { "Custom Exercise" }
        return if (grip == null) base else "$base ($grip)"
    }

    private fun buildGripLabel(
        gripWidth: GripWidth?,
        gripOrientation: GripOrientation?,
    ): String? {
        val labels = buildList {
            gripWidth?.takeUnless { it == GripWidth.NONE || it == GripWidth.STANDARD }?.let { add(it.label()) }
            gripOrientation?.takeUnless { it == GripOrientation.NONE || it == GripOrientation.PRONATED }?.let { add(it.label()) }
        }
        return labels.takeIf { it.isNotEmpty() }?.joinToString(", ")
    }

    private fun Equipment.label(): String =
        when (this) {
            Equipment.BARBELL -> "Barbell"
            Equipment.DUMBBELL -> "Dumbbell"
            Equipment.CABLE -> "Cable"
            Equipment.SMITH_MACHINE -> "Smith Machine"
            Equipment.MACHINE -> "Machine"
            Equipment.KETTLEBELL -> "Kettlebell"
            Equipment.BODYWEIGHT -> "Bodyweight"
            Equipment.BAND -> "Band"
            Equipment.OTHER -> "Other"
        }

    private fun BodyPosition.label(): String =
        when (this) {
            BodyPosition.FLAT_BENCH -> "Flat Bench"
            BodyPosition.INCLINE -> "Incline"
            BodyPosition.DECLINE -> "Decline"
            BodyPosition.SEATED -> "Seated"
            BodyPosition.STANDING -> "Standing"
            BodyPosition.LYING -> "Lying"
            BodyPosition.KNEELING -> "Kneeling"
            BodyPosition.UNILATERAL -> "Unilateral"
            BodyPosition.NONE -> ""
        }

    private fun PrimaryMovement.label(): String =
        when (this) {
            PrimaryMovement.BENCH_PRESS -> "Bench Press"
            PrimaryMovement.OVERHEAD_PRESS -> "Overhead Press"
            PrimaryMovement.ROW -> "Row"
            PrimaryMovement.PULLDOWN -> "Pulldown"
            PrimaryMovement.SQUAT -> "Squat"
            PrimaryMovement.DEADLIFT -> "Deadlift"
            PrimaryMovement.LUNGE -> "Lunge"
            PrimaryMovement.CURL -> "Curl"
            PrimaryMovement.EXTENSION -> "Extension"
            PrimaryMovement.RAISE -> "Raise"
            PrimaryMovement.FLY -> "Fly"
            PrimaryMovement.CARRY -> "Carry"
            PrimaryMovement.OTHER -> "Other"
        }

    private fun GripWidth.label(): String =
        when (this) {
            GripWidth.CLOSE -> "Close Grip"
            GripWidth.STANDARD -> "Standard Grip"
            GripWidth.WIDE -> "Wide Grip"
            GripWidth.NONE -> ""
        }

    private fun GripOrientation.label(): String =
        when (this) {
            GripOrientation.PRONATED -> "Overhand"
            GripOrientation.SUPINATED -> "Supinated"
            GripOrientation.NEUTRAL -> "Neutral Grip"
            GripOrientation.NONE -> ""
        }
}
