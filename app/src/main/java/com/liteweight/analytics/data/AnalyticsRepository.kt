package com.liteweight.analytics.data

import com.liteweight.analytics.data.local.ExercisePrEventEntity
import com.liteweight.analytics.data.local.ExerciseSessionSnapshotEntity
import com.liteweight.analytics.domain.ChartMetric
import com.liteweight.analytics.domain.ChartPoint
import com.liteweight.analytics.domain.ChartWindow
import com.liteweight.analytics.domain.ExerciseAnalyticsSnapshot
import com.liteweight.analytics.domain.MetricsCalculator
import com.liteweight.analytics.domain.PrEvent
import com.liteweight.analytics.domain.PrType
import com.liteweight.analytics.domain.SessionMetrics
import com.liteweight.analytics.domain.sinceEpochMs
import com.liteweight.core.data.LiteWeightDatabase
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class AnalyticsRepository
    @Inject
    constructor(
        private val database: LiteWeightDatabase,
    ) {
        private val analyticsDao get() = database.analyticsDao()
        private val exerciseDao get() = database.exerciseKindDao()
        private val sessionDao get() = database.workoutSessionDao()

        fun observePrTimeline(exerciseKindId: Long): Flow<List<PrEvent>> =
            analyticsDao.observePrTimeline(exerciseKindId).map { rows -> rows.map { it.toEvent() } }

        suspend fun getAnalyticsSnapshot(
            exerciseKindId: Long,
            chartMetric: ChartMetric,
            chartWindow: ChartWindow,
        ): ExerciseAnalyticsSnapshot? {
            val exercise = exerciseDao.getById(exerciseKindId) ?: return null
            val sinceMs = chartWindow.sinceEpochMs()
            val snapshots = analyticsDao.getSnapshots(exerciseKindId, sinceMs)
            val summary = analyticsDao.getSummary(exerciseKindId)
            val prSessionIds =
                chartMetric.toPrType()?.let { prType ->
                    analyticsDao.getPrSessionIds(exerciseKindId, prType).toSet()
                } ?: emptySet()

            val chartPoints =
                snapshots.mapNotNull { snapshot ->
                    val value = snapshot.metricValue(chartMetric) ?: return@mapNotNull null
                    ChartPoint(
                        sessionId = snapshot.sessionId,
                        completedAtEpochMs = snapshot.completedAtEpochMs,
                        value = value,
                        isPr = prSessionIds.contains(snapshot.sessionId),
                    )
                }

            return ExerciseAnalyticsSnapshot(
                exerciseKindId = exerciseKindId,
                unitType = exercise.unitType,
                sessionCount = summary?.sessionCount ?: 0,
                lastSessionAtEpochMs = summary?.lastSessionAtEpochMs,
                bestLoad = summary?.bestLoad,
                bestLoadAtEpochMs = summary?.bestLoadAtEpochMs,
                bestVolume = summary?.bestVolume,
                bestE1rm = summary?.bestE1rm,
                bestReps = summary?.bestReps,
                chartPoints = chartPoints,
                prTimeline = analyticsDao.getPrTimeline(exerciseKindId).map { it.toEvent() },
            )
        }

        suspend fun countCompletedSessionsSince(sinceMs: Long): Int =
            sessionDao.countCompletedSince(sinceMs)
    }

private fun ExerciseSessionSnapshotEntity.metricValue(metric: ChartMetric): Double? {
    val sessionMetrics =
        SessionMetrics(
            topSetLoad = topSetLoad,
            sessionVolume = sessionVolume,
            estimatedOneRm = estimatedOneRm,
            topSetReps = topSetReps,
        )
    return MetricsCalculator.metricValue(sessionMetrics, metric)
}

private fun ChartMetric.toPrType(): PrType? =
    when (this) {
        ChartMetric.TOP_SET_LOAD -> PrType.MAX_LOAD
        ChartMetric.SESSION_VOLUME -> PrType.MAX_VOLUME
        ChartMetric.ESTIMATED_1RM -> PrType.MAX_E1RM
        ChartMetric.TOP_SET_REPS -> PrType.MAX_REPS
    }

private fun ExercisePrEventEntity.toEvent() =
    PrEvent(
        id = id,
        prType = prType,
        value = value,
        achievedAtEpochMs = achievedAtEpochMs,
        sessionId = sessionId,
    )
