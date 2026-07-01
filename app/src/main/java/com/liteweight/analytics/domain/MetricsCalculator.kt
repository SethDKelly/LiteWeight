package com.liteweight.analytics.domain

object MetricsCalculator {
    fun computeSessionMetrics(sets: List<SessionSetInput>): SessionMetrics {
        val working =
            sets.filter { it.isCompleted && !it.isWarmup }
        if (working.isEmpty()) {
            return SessionMetrics(null, null, null, null)
        }

        val loadSets = working.filter { it.load != null && it.reps != null }
        val topLoadSet =
            loadSets.maxByOrNull { it.load ?: 0.0 }
        val topSetLoad = topLoadSet?.load
        val topSetReps = topLoadSet?.reps ?: working.maxOfOrNull { it.reps ?: 0 }

        val sessionVolume =
            loadSets.sumOf { (it.load ?: 0.0) * (it.reps ?: 0) }
                .takeIf { it > 0.0 }

        val estimatedOneRm =
            loadSets
                .mapNotNull { set ->
                    val load = set.load ?: return@mapNotNull null
                    val reps = set.reps ?: return@mapNotNull null
                    estimatedOneRm(load, reps)
                }.maxOrNull()

        return SessionMetrics(
            topSetLoad = topSetLoad,
            sessionVolume = sessionVolume,
            estimatedOneRm = estimatedOneRm,
            topSetReps = topSetReps,
        )
    }

    fun estimatedOneRm(
        load: Double,
        reps: Int,
    ): Double = load * (1.0 + reps / 30.0)

    fun metricValue(
        metrics: SessionMetrics,
        chartMetric: ChartMetric,
    ): Double? =
        when (chartMetric) {
            ChartMetric.TOP_SET_LOAD -> metrics.topSetLoad
            ChartMetric.SESSION_VOLUME -> metrics.sessionVolume
            ChartMetric.ESTIMATED_1RM -> metrics.estimatedOneRm
            ChartMetric.TOP_SET_REPS -> metrics.topSetReps?.toDouble()
        }
}
