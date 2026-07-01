package com.liteweight.generator.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class RoutineGeneratorEngineTest {
    @Test
    fun generatesThreeDayFullBody() {
        val draft =
            RoutineGeneratorEngine.generate(
                GeneratorInput(3, TrainingGoal.GENERAL, EquipmentProfile.FULL_GYM),
            )
        assertEquals(3, draft.days.size)
        assertEquals("FullBody", draft.splitType)
        assertEquals(4, draft.days.first().exercises.size)
    }

    @Test
    fun generatesFourDayUpperLower() {
        val draft =
            RoutineGeneratorEngine.generate(
                GeneratorInput(4, TrainingGoal.HYPERTROPHY, EquipmentProfile.FULL_GYM),
            )
        assertEquals(4, draft.days.size)
        assertEquals("UpperLower", draft.splitType)
    }

    @Test
    fun fiveDaysUsesPplTemplate() {
        val draft =
            RoutineGeneratorEngine.generate(
                GeneratorInput(5, TrainingGoal.STRENGTH, EquipmentProfile.FULL_GYM),
            )
        assertEquals(5, draft.days.size)
        assertEquals("PushPullLegs", draft.splitType)
    }
}
