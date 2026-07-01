package com.liteweight.progression.domain

object ProgressionEvaluator {
    fun shouldAdvanceBySessionCount(
        sessionsAtLevel: Int,
        sessionsRequired: Int?,
    ): Boolean = sessionsRequired != null && sessionsRequired > 0 && sessionsAtLevel >= sessionsRequired

    fun shouldAdvanceByRepTarget(
        completedSets: List<SetPerformance>,
        prescriptions: List<ExercisePrescription>,
        levelRepMin: Int?,
    ): Boolean {
        val repMin = levelRepMin ?: return false
        if (prescriptions.isEmpty()) return false
        return prescriptions.all { prescription ->
            val workingSets =
                completedSets.filter { set ->
                    !set.isWarmup &&
                        set.isCompleted &&
                        matchesPrescription(set, prescription.exerciseKindId)
                }
            workingSets.size >= prescription.targetSets &&
                workingSets.all { (it.repCount ?: 0) >= repMin }
        }
    }

    fun evaluateAdvancement(
        rule: AdvancementRuleType,
        sessionsAtLevel: Int,
        sessionsRequired: Int?,
        completedSets: List<SetPerformance>,
        prescriptions: List<ExercisePrescription>,
        levelRepMin: Int?,
    ): Boolean =
        when (rule) {
            AdvancementRuleType.MANUAL_ONLY -> false
            AdvancementRuleType.SESSION_COUNT ->
                shouldAdvanceBySessionCount(sessionsAtLevel, sessionsRequired)
            AdvancementRuleType.REP_TARGET ->
                shouldAdvanceByRepTarget(completedSets, prescriptions, levelRepMin)
        }

    fun nextLevelIndex(
        current: Int,
        levelCount: Int,
    ): Int = if (levelCount <= 0) 0 else (current + 1) % levelCount

    fun adjustedLoad(
        lastLoad: Double?,
        loadIncrement: Double,
        isDeload: Boolean,
    ): Double? {
        if (lastLoad == null) return null
        return if (isDeload) {
            (lastLoad * 0.9).coerceAtLeast(0.0)
        } else {
            (lastLoad + loadIncrement).coerceAtLeast(0.0)
        }
    }

    private fun matchesPrescription(
        set: SetPerformance,
        prescriptionExerciseKindId: Long,
    ): Boolean =
        set.exerciseKindId == prescriptionExerciseKindId ||
            set.substitutedFromExerciseKindId == prescriptionExerciseKindId
}
