package com.liteweight.core.navigation

object LiteWeightDestinations {
    const val HOME = "home"
    const val EXERCISES = "exercises"
    const val EXERCISE_DETAIL = "exercise_detail/{exerciseId}"
    const val CREATE_EXERCISE = "create_exercise"
    const val ACTIVE_WORKOUT = "active_workout/{sessionId}"
    const val SESSION_HISTORY = "session_history"
    const val SETTINGS = "settings"
    const val PROGRAMS = "programs"
    const val CREATE_PROGRAM = "create_program"
    const val PROGRAM_DETAIL = "program_detail/{programId}"
    const val PRESETS = "presets"
    const val PROGRESSION_SCHEMES = "progression_schemes"
    const val PROGRESSION_SCHEME_DETAIL = "progression_scheme/{schemeId}"
    const val ROTATIONS = "rotations"
    const val CREATE_ROTATION = "create_rotation"
    const val ROTATION_DETAIL = "rotation_detail/{planId}"
    const val ROUTINE_GENERATOR = "routine_generator"
    const val SUBSTITUTION_GROUPS = "substitution_groups"
    const val SUBSTITUTION_GROUP_DETAIL = "substitution_group/{groupId}"

    fun exerciseDetail(exerciseId: Long) = "exercise_detail/$exerciseId"

    fun activeWorkout(sessionId: Long) = "active_workout/$sessionId"

    fun programDetail(programId: Long) = "program_detail/$programId"

    fun progressionSchemeDetail(schemeId: Long) = "progression_scheme/$schemeId"

    fun rotationDetail(planId: Long) = "rotation_detail/$planId"

    fun substitutionGroupDetail(groupId: Long) = "substitution_group/$groupId"
}
