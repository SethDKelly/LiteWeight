package com.liteweight.analytics.domain

data class PrCandidate(
    val prType: PrType,
    val value: Double,
    val sessionId: Long,
    val setId: Long?,
    val achievedAtEpochMs: Long,
)

object PrDetector {
    fun candidatesFromSession(
        sessionId: Long,
        completedAtEpochMs: Long,
        metrics: SessionMetrics,
        topSetId: Long?,
    ): List<PrCandidate> {
        val candidates = mutableListOf<PrCandidate>()
        metrics.topSetLoad?.let { load ->
            candidates +=
                PrCandidate(
                    prType = PrType.MAX_LOAD,
                    value = load,
                    sessionId = sessionId,
                    setId = topSetId,
                    achievedAtEpochMs = completedAtEpochMs,
                )
        }
        metrics.sessionVolume?.let { volume ->
            candidates +=
                PrCandidate(
                    prType = PrType.MAX_VOLUME,
                    value = volume,
                    sessionId = sessionId,
                    setId = null,
                    achievedAtEpochMs = completedAtEpochMs,
                )
        }
        metrics.estimatedOneRm?.let { e1rm ->
            candidates +=
                PrCandidate(
                    prType = PrType.MAX_E1RM,
                    value = e1rm,
                    sessionId = sessionId,
                    setId = topSetId,
                    achievedAtEpochMs = completedAtEpochMs,
                )
        }
        metrics.topSetReps?.let { reps ->
            candidates +=
                PrCandidate(
                    prType = PrType.MAX_REPS,
                    value = reps.toDouble(),
                    sessionId = sessionId,
                    setId = topSetId,
                    achievedAtEpochMs = completedAtEpochMs,
                )
        }
        return candidates
    }

    fun shouldReplace(
        existingValue: Double?,
        existingAt: Long?,
        candidate: PrCandidate,
    ): Boolean {
        if (existingValue == null) return true
        if (candidate.value > existingValue) return true
        if (candidate.value == existingValue && existingAt != null && candidate.achievedAtEpochMs > existingAt) {
            return true
        }
        return false
    }
}
