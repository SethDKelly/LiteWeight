package com.liteweight.analytics.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MetricsCalculatorTest {
    @Test
    fun computesTopLoadVolumeAndE1rm() {
        val metrics =
            MetricsCalculator.computeSessionMetrics(
                listOf(
                    SessionSetInput(load = 80.0, reps = 8, isWarmup = false, isCompleted = true),
                    SessionSetInput(load = 90.0, reps = 5, isWarmup = false, isCompleted = true),
                    SessionSetInput(load = 60.0, reps = 12, isWarmup = true, isCompleted = true),
                ),
            )

        assertEquals(90.0, metrics.topSetLoad!!, 0.001)
        assertEquals(5, metrics.topSetReps)
        assertEquals(1090.0, metrics.sessionVolume!!, 0.001)
        assertEquals(105.0, metrics.estimatedOneRm!!, 0.001)
    }

    @Test
    fun emptyWorkingSetsReturnNullMetrics() {
        val metrics =
            MetricsCalculator.computeSessionMetrics(
                listOf(SessionSetInput(load = 50.0, reps = 10, isWarmup = true, isCompleted = true)),
            )
        assertNull(metrics.topSetLoad)
        assertNull(metrics.sessionVolume)
    }

    @Test
    fun epleyFormulaMatchesDocumentedFormula() {
        assertEquals(100.0, MetricsCalculator.estimatedOneRm(100.0, 0), 0.001)
        assertEquals(110.0, MetricsCalculator.estimatedOneRm(100.0, 3), 0.001)
    }
}
