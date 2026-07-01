package com.liteweight.analytics.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrDetectorTest {
    @Test
    fun replacesWhenValueIsHigher() {
        val candidate =
            PrCandidate(
                prType = PrType.MAX_LOAD,
                value = 100.0,
                sessionId = 2,
                setId = 1,
                achievedAtEpochMs = 2000,
            )
        assertTrue(PrDetector.shouldReplace(90.0, 1000, candidate))
    }

    @Test
    fun tieBreakPrefersMostRecent() {
        val candidate =
            PrCandidate(
                prType = PrType.MAX_LOAD,
                value = 100.0,
                sessionId = 2,
                setId = 1,
                achievedAtEpochMs = 2000,
            )
        assertTrue(PrDetector.shouldReplace(100.0, 1000, candidate))
        assertFalse(
            PrDetector.shouldReplace(
                100.0,
                3000,
                candidate.copy(achievedAtEpochMs = 1500),
            ),
        )
    }

    @Test
    fun doesNotReplaceWhenValueIsLower() {
        val candidate =
            PrCandidate(
                prType = PrType.MAX_LOAD,
                value = 80.0,
                sessionId = 2,
                setId = 1,
                achievedAtEpochMs = 2000,
            )
        assertFalse(PrDetector.shouldReplace(100.0, 1000, candidate))
    }
}
