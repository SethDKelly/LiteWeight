package com.liteweight.generator.domain

object RoutineGeneratorEngine {
    fun generate(input: GeneratorInput): ProgramDraft {
        require(input.daysPerWeek in 2..6)
        return when (input.daysPerWeek) {
            3 -> fullBodyThreeDay(input)
            4 -> upperLowerFourDay(input)
            else -> pplFiveDay(input)
        }
    }

    private fun fullBodyThreeDay(input: GeneratorInput): ProgramDraft {
        val sets = if (input.goal == TrainingGoal.STRENGTH) 4 else 3
        val (minReps, maxReps) = repRange(input.goal)
        val catalog = catalogFor(input.equipment)
        val dayExercises =
            listOf(
                ex("main_squat", catalog.squat, sets, minReps, maxReps),
                ex("horizontal_push", catalog.hPush, sets, minReps, maxReps),
                ex("horizontal_pull", catalog.hPull, sets, minReps, maxReps),
                ex("main_hinge", catalog.hinge, sets, minReps, maxReps),
            )
        return ProgramDraft(
            name = "Generated Full Body ${input.daysPerWeek}×",
            splitType = "FullBody",
            days =
                (1..3).map { index ->
                    DraftDay("Full Body $index", dayExercises)
                },
        )
    }

    private fun upperLowerFourDay(input: GeneratorInput): ProgramDraft {
        val sets = if (input.goal == TrainingGoal.STRENGTH) 4 else 3
        val (minReps, maxReps) = repRange(input.goal)
        val catalog = catalogFor(input.equipment)
        val upper =
            listOf(
                ex("horizontal_push", catalog.hPush, sets, minReps, maxReps),
                ex("vertical_push", catalog.vPush, sets, minReps, maxReps),
                ex("horizontal_pull", catalog.hPull, sets, minReps, maxReps),
                ex("vertical_pull", catalog.vPull, sets, minReps, maxReps),
            )
        val lower =
            listOf(
                ex("main_squat", catalog.squat, sets, minReps, maxReps),
                ex("main_hinge", catalog.hinge, sets, minReps, maxReps),
                ex("quad_accessory", catalog.quadAcc, 3, 10, 15),
                ex("hamstring_accessory", catalog.hamAcc, 3, 10, 15),
            )
        return ProgramDraft(
            name = "Generated Upper/Lower",
            splitType = "UpperLower",
            days =
                listOf(
                    DraftDay("Upper A", upper),
                    DraftDay("Lower A", lower),
                    DraftDay("Upper B", upper),
                    DraftDay("Lower B", lower),
                ),
        )
    }

    private fun pplFiveDay(input: GeneratorInput): ProgramDraft {
        val sets = 3
        val (minReps, maxReps) = repRange(input.goal)
        val catalog = catalogFor(input.equipment)
        val push =
            listOf(
                ex("horizontal_push", catalog.hPush, sets, minReps, maxReps),
                ex("vertical_push", catalog.vPush, sets, minReps, maxReps),
                ex("arm_accessory", catalog.arm, 3, 10, 12),
            )
        val pull =
            listOf(
                ex("horizontal_pull", catalog.hPull, sets, minReps, maxReps),
                ex("vertical_pull", catalog.vPull, sets, minReps, maxReps),
                ex("main_hinge", catalog.hinge, sets, minReps, maxReps),
            )
        val legs =
            listOf(
                ex("main_squat", catalog.squat, sets, minReps, maxReps),
                ex("quad_accessory", catalog.quadAcc, 3, 10, 15),
                ex("hamstring_accessory", catalog.hamAcc, 3, 10, 15),
            )
        return ProgramDraft(
            name = "Generated PPL",
            splitType = "PushPullLegs",
            days =
                listOf(
                    DraftDay("Push", push),
                    DraftDay("Pull", pull),
                    DraftDay("Legs", legs),
                    DraftDay("Push B", push),
                    DraftDay("Pull B", pull),
                ),
        )
    }

    private fun repRange(goal: TrainingGoal): Pair<Int, Int> =
        when (goal) {
            TrainingGoal.STRENGTH -> 4 to 6
            TrainingGoal.HYPERTROPHY -> 8 to 12
            TrainingGoal.GENERAL -> 8 to 10
        }

    private fun ex(
        slot: String,
        catalogId: String,
        sets: Int,
        min: Int,
        max: Int,
    ) = DraftExercise(slot, catalogId, sets, min, max)

    private data class CatalogPick(
        val squat: String,
        val hinge: String,
        val hPush: String,
        val vPush: String,
        val hPull: String,
        val vPull: String,
        val quadAcc: String,
        val hamAcc: String,
        val arm: String,
    )

    private fun catalogFor(equipment: EquipmentProfile): CatalogPick =
        when (equipment) {
            EquipmentProfile.FULL_GYM ->
                CatalogPick(
                    squat = "lw:barbell-squat",
                    hinge = "lw:conventional-deadlift",
                    hPush = "lw:barbell-bench-press",
                    vPush = "lw:overhead-press",
                    hPull = "lw:barbell-row",
                    vPull = "lw:lat-pulldown",
                    quadAcc = "lw:leg-extension",
                    hamAcc = "lw:leg-curl",
                    arm = "lw:barbell-curl",
                )
            EquipmentProfile.HOME_DUMBBELLS ->
                CatalogPick(
                    squat = "lw:dumbbell-lunge",
                    hinge = "lw:romanian-deadlift",
                    hPush = "lw:dumbbell-incline-press",
                    vPush = "lw:overhead-press",
                    hPull = "lw:seated-cable-row",
                    vPull = "lw:lat-pulldown",
                    quadAcc = "lw:dumbbell-lunge",
                    hamAcc = "lw:romanian-deadlift",
                    arm = "lw:barbell-curl",
                )
            EquipmentProfile.BODYWEIGHT ->
                CatalogPick(
                    squat = "lw:push-up",
                    hinge = "lw:push-up",
                    hPush = "lw:push-up",
                    vPush = "lw:push-up",
                    hPull = "lw:pull-up",
                    vPull = "lw:pull-up",
                    quadAcc = "lw:push-up",
                    hamAcc = "lw:push-up",
                    arm = "lw:push-up",
                )
        }
}
