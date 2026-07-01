package com.liteweight.rotation.domain

object RotationResolver {
    private const val DAY_MS = 24L * 60 * 60 * 1000
    private const val WEEK_MS = 7 * DAY_MS
    private const val MONTH_MS = 30 * DAY_MS

    fun resolve(
        cadenceType: CadenceType,
        cadenceInterval: Int,
        anchorEpochMs: Long,
        slotProgramIds: List<Long>,
        slotLabels: List<String?>,
        programNames: Map<Long, String>,
        nowEpochMs: Long,
    ): RotationResolution? {
        if (slotProgramIds.isEmpty() || cadenceInterval <= 0) return null

        val periodMs = periodLengthMs(cadenceType, cadenceInterval)
        val elapsed = (nowEpochMs - anchorEpochMs).coerceAtLeast(0)
        val periodIndex = (elapsed / periodMs).toInt()
        val currentIndex = periodIndex % slotProgramIds.size
        val nextIndex = (currentIndex + 1) % slotProgramIds.size
        val periodStart = anchorEpochMs + periodIndex * periodMs
        val periodEndsAt = periodStart + periodMs

        val currentProgramId = slotProgramIds[currentIndex]
        val nextProgramId = slotProgramIds[nextIndex]

        return RotationResolution(
            planId = 0,
            planName = "",
            currentSlotIndex = currentIndex,
            currentProgramId = currentProgramId,
            currentProgramName = programNames[currentProgramId] ?: "Program",
            currentLabel = slotLabels.getOrNull(currentIndex),
            nextProgramId = nextProgramId,
            nextProgramName = programNames[nextProgramId] ?: "Program",
            nextLabel = slotLabels.getOrNull(nextIndex),
            periodEndsAtEpochMs = periodEndsAt,
        )
    }

    fun periodLengthMs(
        cadenceType: CadenceType,
        cadenceInterval: Int,
    ): Long =
        when (cadenceType) {
            CadenceType.DAYS -> cadenceInterval * DAY_MS
            CadenceType.WEEKS -> cadenceInterval * WEEK_MS
            CadenceType.MONTHS -> cadenceInterval * MONTH_MS
        }
}
