package com.liteweight.exercise.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ExerciseDisplayNameBuilderTest {
    @Test
    fun buildsStructuredNameWithGripModifiers() {
        val name =
            ExerciseDisplayNameBuilder.build(
                equipment = Equipment.BARBELL,
                bodyPosition = BodyPosition.INCLINE,
                primaryMovement = PrimaryMovement.BENCH_PRESS,
                primaryMovementFreeform = null,
                gripWidth = GripWidth.CLOSE,
                gripOrientation = GripOrientation.SUPINATED,
                customDisplayName = null,
            )

        assertEquals("Barbell Incline Bench Press (Close Grip, Supinated)", name)
    }

    @Test
    fun usesCustomDisplayNameWhenProvided() {
        val name =
            ExerciseDisplayNameBuilder.build(
                equipment = Equipment.DUMBBELL,
                bodyPosition = null,
                primaryMovement = PrimaryMovement.CURL,
                primaryMovementFreeform = null,
                gripWidth = null,
                gripOrientation = null,
                customDisplayName = "  Custom Curl  ",
            )

        assertEquals("Custom Curl", name)
    }
}
