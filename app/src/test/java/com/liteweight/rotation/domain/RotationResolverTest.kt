package com.liteweight.rotation.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class RotationResolverTest {
    private val anchor = 0L
    private val weekMs = 7L * 24 * 60 * 60 * 1000
    private val programs = listOf(10L, 20L, 30L)
    private val names = mapOf(10L to "A", 20L to "B", 30L to "C")

    @Test
    fun resolvesFirstSlotAtAnchor() {
        val result =
            RotationResolver.resolve(
                cadenceType = CadenceType.WEEKS,
                cadenceInterval = 2,
                anchorEpochMs = anchor,
                slotProgramIds = programs,
                slotLabels = listOf("Meso 1", null, null),
                programNames = names,
                nowEpochMs = anchor,
            )!!
        assertEquals(10L, result.currentProgramId)
        assertEquals("A", result.currentProgramName)
        assertEquals(20L, result.nextProgramId)
    }

    @Test
    fun advancesAfterCadencePeriod() {
        val result =
            RotationResolver.resolve(
                cadenceType = CadenceType.WEEKS,
                cadenceInterval = 1,
                anchorEpochMs = anchor,
                slotProgramIds = programs,
                slotLabels = listOf(null, null, null),
                programNames = names,
                nowEpochMs = anchor + weekMs,
            )!!
        assertEquals(20L, result.currentProgramId)
        assertEquals(30L, result.nextProgramId)
    }

    @Test
    fun wrapsAroundAtEndOfSlots() {
        val result =
            RotationResolver.resolve(
                cadenceType = CadenceType.WEEKS,
                cadenceInterval = 1,
                anchorEpochMs = anchor,
                slotProgramIds = programs,
                slotLabels = listOf(null, null, null),
                programNames = names,
                nowEpochMs = anchor + weekMs * 3,
            )!!
        assertEquals(10L, result.currentProgramId)
    }
}
