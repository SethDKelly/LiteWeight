package com.liteweight.analytics.domain

import com.liteweight.exercise.domain.UnitType

enum class ChartMetric {
    TOP_SET_LOAD,
    SESSION_VOLUME,
    ESTIMATED_1RM,
    TOP_SET_REPS,
}

enum class ChartWindow {
    WEEKS_4,
    WEEKS_12,
    MONTHS_6,
    ALL,
}

enum class PrType {
    MAX_LOAD,
    MAX_VOLUME,
    MAX_E1RM,
    MAX_REPS,
}

data class SessionSetInput(
    val load: Double?,
    val reps: Int?,
    val isWarmup: Boolean,
    val isCompleted: Boolean,
)

data class SessionMetrics(
    val topSetLoad: Double?,
    val sessionVolume: Double?,
    val estimatedOneRm: Double?,
    val topSetReps: Int?,
)

data class ChartPoint(
    val sessionId: Long,
    val completedAtEpochMs: Long,
    val value: Double,
    val isPr: Boolean,
)

data class PrEvent(
    val id: Long,
    val prType: PrType,
    val value: Double,
    val achievedAtEpochMs: Long,
    val sessionId: Long,
)

data class ExerciseAnalyticsSnapshot(
    val exerciseKindId: Long,
    val unitType: UnitType,
    val sessionCount: Int,
    val lastSessionAtEpochMs: Long?,
    val bestLoad: Double?,
    val bestLoadAtEpochMs: Long?,
    val bestVolume: Double?,
    val bestE1rm: Double?,
    val bestReps: Int?,
    val chartPoints: List<ChartPoint>,
    val prTimeline: List<PrEvent>,
)

fun ChartWindow.sinceEpochMs(nowMs: Long = System.currentTimeMillis()): Long? =
    when (this) {
        ChartWindow.WEEKS_4 -> nowMs - 28L * 24 * 60 * 60 * 1000
        ChartWindow.WEEKS_12 -> nowMs - 84L * 24 * 60 * 60 * 1000
        ChartWindow.MONTHS_6 -> nowMs - 183L * 24 * 60 * 60 * 1000
        ChartWindow.ALL -> null
    }

fun ChartMetric.label(unitType: UnitType): String =
    when (this) {
        ChartMetric.TOP_SET_LOAD -> if (unitType == UnitType.BODYWEIGHT) "Top reps" else "Top set load"
        ChartMetric.SESSION_VOLUME -> "Volume"
        ChartMetric.ESTIMATED_1RM -> "Est. 1RM"
        ChartMetric.TOP_SET_REPS -> "Top reps"
    }

fun availableMetrics(unitType: UnitType): List<ChartMetric> =
    when (unitType) {
        UnitType.WEIGHT -> listOf(ChartMetric.TOP_SET_LOAD, ChartMetric.SESSION_VOLUME, ChartMetric.ESTIMATED_1RM)
        UnitType.BODYWEIGHT -> listOf(ChartMetric.TOP_SET_REPS, ChartMetric.SESSION_VOLUME)
        UnitType.TIME, UnitType.DISTANCE -> listOf(ChartMetric.TOP_SET_REPS)
    }
