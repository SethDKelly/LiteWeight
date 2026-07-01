package com.liteweight.progression.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressionEvaluatorTest {
    @Test
    fun sessionCountAdvancesWhenThresholdMet() {
        assertTrue(ProgressionEvaluator.shouldAdvanceBySessionCount(3, 3))
        assertFalse(ProgressionEvaluator.shouldAdvanceBySessionCount(2, 3))
    }

    @Test
    fun repTargetRequiresAllPrescribedSetsAtMinimum() {
        val prescriptions =
            listOf(
                ExercisePrescription(exerciseKindId = 1, targetSets = 2, targetRepsMin = 8),
            )
        val passingSets =
            listOf(
                SetPerformance(1, null, 8, false, true),
                SetPerformance(1, null, 10, false, true),
            )
        assertTrue(ProgressionEvaluator.shouldAdvanceByRepTarget(passingSets, prescriptions, 8))

        val failingSets =
            listOf(
                SetPerformance(1, null, 8, false, true),
                SetPerformance(1, null, 7, false, true),
            )
        assertFalse(ProgressionEvaluator.shouldAdvanceByRepTarget(failingSets, prescriptions, 8))
    }

    @Test
    fun repTargetHonorsSubstitutionAuditTrail() {
        val prescriptions =
            listOf(
                ExercisePrescription(exerciseKindId = 1, targetSets = 1, targetRepsMin = 5),
            )
        val swappedSets =
            listOf(
                SetPerformance(exerciseKindId = 2, substitutedFromExerciseKindId = 1, repCount = 6, isWarmup = false, isCompleted = true),
            )
        assertTrue(ProgressionEvaluator.shouldAdvanceByRepTarget(swappedSets, prescriptions, 5))
    }

    @Test
    fun nextLevelIndexWrapsAround() {
        assertEquals(1, ProgressionEvaluator.nextLevelIndex(0, 4))
        assertEquals(0, ProgressionEvaluator.nextLevelIndex(3, 4))
    }

    @Test
    fun adjustedLoadAppliesIncrementOrDeload() {
        assertEquals(102.5, ProgressionEvaluator.adjustedLoad(100.0, 2.5, isDeload = false)!!, 0.001)
        assertEquals(90.0, ProgressionEvaluator.adjustedLoad(100.0, 0.0, isDeload = true)!!, 0.001)
    }

    @Test
    fun manualRuleNeverAutoAdvances() {
        assertFalse(
            ProgressionEvaluator.evaluateAdvancement(
                rule = AdvancementRuleType.MANUAL_ONLY,
                sessionsAtLevel = 99,
                sessionsRequired = 1,
                completedSets = emptyList(),
                prescriptions = emptyList(),
                levelRepMin = 5,
            ),
        )
    }
}
