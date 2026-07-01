package com.liteweight.analytics.data

import com.liteweight.analytics.data.local.AnalyticsDao
import com.liteweight.analytics.data.local.ExerciseAnalyticsSummaryEntity
import com.liteweight.analytics.data.local.ExercisePrEventEntity
import com.liteweight.analytics.data.local.ExerciseSessionSnapshotEntity
import com.liteweight.analytics.domain.MetricsCalculator
import com.liteweight.analytics.domain.PrCandidate
import com.liteweight.analytics.domain.PrDetector
import com.liteweight.analytics.domain.PrType
import com.liteweight.analytics.domain.SessionMetrics
import com.liteweight.analytics.domain.SessionSetInput
import com.liteweight.core.data.LiteWeightDatabase
import com.liteweight.session.data.local.SessionStatus
import com.liteweight.session.data.local.WorkoutSetEntryEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsProjectionService
    @Inject
    constructor(
        private val database: LiteWeightDatabase,
    ) {
        private val analyticsDao get() = database.analyticsDao()
        private val sessionDao get() = database.workoutSessionDao()

        suspend fun recomputeSession(sessionId: Long) {
            val session = sessionDao.getSession(sessionId) ?: return
            if (session.status != SessionStatus.COMPLETED || session.completedAtEpochMs == null) return

            val entries = sessionDao.getSessionExercises(sessionId)
            entries.forEach { entry ->
                val sets = sessionDao.getSets(entry.entryId)
                val metrics = computeMetrics(sets)
                val topSetId = findTopSetId(sets, metrics.topSetLoad)

                analyticsDao.upsertSnapshot(
                    ExerciseSessionSnapshotEntity(
                        exerciseKindId = entry.exerciseKindId,
                        sessionId = sessionId,
                        completedAtEpochMs = session.completedAtEpochMs,
                        topSetLoad = metrics.topSetLoad,
                        sessionVolume = metrics.sessionVolume,
                        estimatedOneRm = metrics.estimatedOneRm,
                        topSetReps = metrics.topSetReps,
                    ),
                )

                recordNewPrs(
                    exerciseKindId = entry.exerciseKindId,
                    sessionId = sessionId,
                    completedAtEpochMs = session.completedAtEpochMs,
                    metrics = metrics,
                    topSetId = topSetId,
                )
                refreshSummary(entry.exerciseKindId)
            }
        }

        private suspend fun recordNewPrs(
            exerciseKindId: Long,
            sessionId: Long,
            completedAtEpochMs: Long,
            metrics: SessionMetrics,
            topSetId: Long?,
        ) {
            val candidates =
                PrDetector.candidatesFromSession(
                    sessionId = sessionId,
                    completedAtEpochMs = completedAtEpochMs,
                    metrics = metrics,
                    topSetId = topSetId,
                )
            candidates.forEach { candidate ->
                val existing = analyticsDao.getBestPr(exerciseKindId, candidate.prType)
                if (
                    PrDetector.shouldReplace(
                        existingValue = existing?.value,
                        existingAt = existing?.achievedAtEpochMs,
                        candidate = candidate,
                    )
                ) {
                    analyticsDao.insertPrEvent(candidate.toEntity(exerciseKindId))
                }
            }
        }

        private suspend fun refreshSummary(exerciseKindId: Long) {
            analyticsDao.upsertSummary(
                ExerciseAnalyticsSummaryEntity(
                    exerciseKindId = exerciseKindId,
                    sessionCount = analyticsDao.countSnapshots(exerciseKindId),
                    lastSessionAtEpochMs = analyticsDao.lastSnapshotAt(exerciseKindId),
                    bestLoad = analyticsDao.getBestPr(exerciseKindId, PrType.MAX_LOAD)?.value,
                    bestLoadAtEpochMs = analyticsDao.getBestPr(exerciseKindId, PrType.MAX_LOAD)?.achievedAtEpochMs,
                    bestVolume = analyticsDao.getBestPr(exerciseKindId, PrType.MAX_VOLUME)?.value,
                    bestE1rm = analyticsDao.getBestPr(exerciseKindId, PrType.MAX_E1RM)?.value,
                    bestReps = analyticsDao.getBestPr(exerciseKindId, PrType.MAX_REPS)?.value?.toInt(),
                ),
            )
        }

        private fun computeMetrics(sets: List<WorkoutSetEntryEntity>): SessionMetrics =
            MetricsCalculator.computeSessionMetrics(
                sets.map { set ->
                    SessionSetInput(
                        load = set.loadValue,
                        reps = set.repCount,
                        isWarmup = set.isWarmup,
                        isCompleted = set.isCompleted,
                    )
                },
            )

        private fun findTopSetId(
            sets: List<WorkoutSetEntryEntity>,
            topLoad: Double?,
        ): Long? {
            if (topLoad == null) return null
            return sets
                .filter { it.isCompleted && !it.isWarmup && it.loadValue == topLoad }
                .maxByOrNull { it.setIndex }
                ?.id
        }

        private fun PrCandidate.toEntity(exerciseKindId: Long) =
            ExercisePrEventEntity(
                exerciseKindId = exerciseKindId,
                prType = prType,
                value = value,
                sessionId = sessionId,
                setId = setId,
                achievedAtEpochMs = achievedAtEpochMs,
            )
    }
