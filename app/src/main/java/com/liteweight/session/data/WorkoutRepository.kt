package com.liteweight.session.data

import com.liteweight.core.data.LiteWeightDatabase
import com.liteweight.core.data.UserPreferencesRepository
import com.liteweight.analytics.data.AnalyticsProjectionService
import com.liteweight.program.data.ProgramRepository
import com.liteweight.progression.data.ProgressionRepository
import com.liteweight.session.data.local.SessionExerciseWithName
import com.liteweight.session.data.local.SessionStatus
import com.liteweight.session.data.local.SessionSummaryRow
import com.liteweight.session.data.local.WorkoutExerciseEntryEntity
import com.liteweight.session.data.local.WorkoutSessionEntity
import com.liteweight.session.data.local.WorkoutSetEntryEntity
import com.liteweight.session.domain.SessionSummary
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class WorkoutRepository
    @Inject
    constructor(
        private val database: LiteWeightDatabase,
        private val programRepository: ProgramRepository,
        private val progressionRepository: ProgressionRepository,
        private val analyticsProjectionService: AnalyticsProjectionService,
        private val sessionPrefillService: SessionPrefillService,
        private val userPreferencesRepository: UserPreferencesRepository,
    ) {
        private val sessionDao get() = database.workoutSessionDao()

        fun observeCompletedSessions(): Flow<List<SessionSummary>> =
            sessionDao.observeCompletedSummaries().map { rows -> rows.map { it.toSummary() } }

        fun observeSessionExercises(sessionId: Long): Flow<List<SessionExerciseWithName>> =
            sessionDao.observeSessionExercises(sessionId)

        fun observeSets(entryId: Long): Flow<List<WorkoutSetEntryEntity>> = sessionDao.observeSets(entryId)

        suspend fun getSession(id: Long): WorkoutSessionEntity? = sessionDao.getSession(id)

        suspend fun startOrResumeDraft(): Long {
            val existing = sessionDao.getLatestDraft()
            if (existing != null) return existing.id
            return createDraftSession(prefillFromProgram = true)
        }

        suspend fun startFreshWorkout(): Long = createDraftSession(prefillFromProgram = false)

        private suspend fun createDraftSession(prefillFromProgram: Boolean): Long {
            val now = System.currentTimeMillis()
            val sessionId =
                sessionDao.insertSession(
                    WorkoutSessionEntity(
                        status = SessionStatus.DRAFT,
                        startedAtEpochMs = now,
                        completedAtEpochMs = null,
                        notes = null,
                    ),
                )
            if (prefillFromProgram) {
                val exercises = programRepository.getExercisesForActiveDay()
                if (exercises.isNotEmpty()) {
                    sessionPrefillService.prefillSession(
                        sessionId = sessionId,
                        exercises = exercises,
                        mode = userPreferencesRepository.getPrefillMode(),
                    )
                }
            }
            return sessionId
        }

        suspend fun addExercise(sessionId: Long, exerciseKindId: Long): Long {
            val sortOrder = sessionDao.maxExerciseSortOrder(sessionId) + 1
            return sessionDao.insertExerciseEntry(
                WorkoutExerciseEntryEntity(
                    sessionId = sessionId,
                    exerciseKindId = exerciseKindId,
                    movementSlotId = null,
                    sortOrder = sortOrder,
                    substitutedFromExerciseKindId = null,
                ),
            )
        }

        suspend fun swapExercise(entryId: Long, newExerciseKindId: Long) {
            val entry = sessionDao.getExerciseEntry(entryId) ?: return
            sessionDao.updateExerciseEntry(
                entry.copy(
                    exerciseKindId = newExerciseKindId,
                    substitutedFromExerciseKindId = entry.exerciseKindId,
                ),
            )
        }

        suspend fun swapExerciseWithSlot(
            entryId: Long,
            newExerciseKindId: Long,
            movementSlotId: String?,
        ) {
            val entry = sessionDao.getExerciseEntry(entryId) ?: return
            sessionDao.updateExerciseEntry(
                entry.copy(
                    exerciseKindId = newExerciseKindId,
                    movementSlotId = movementSlotId ?: entry.movementSlotId,
                    substitutedFromExerciseKindId = entry.exerciseKindId,
                ),
            )
        }

        suspend fun addSet(entryId: Long, load: Double?, reps: Int?): Long {
            val setIndex = sessionDao.maxSetIndex(entryId) + 1
            return sessionDao.insertSet(
                WorkoutSetEntryEntity(
                    workoutExerciseEntryId = entryId,
                    setIndex = setIndex,
                    loadValue = load,
                    repCount = reps,
                    isWarmup = false,
                    isCompleted = true,
                ),
            )
        }

        suspend fun completeSession(sessionId: Long) {
            val session = sessionDao.getSession(sessionId) ?: return
            sessionDao.updateSession(
                session.copy(
                    status = SessionStatus.COMPLETED,
                    completedAtEpochMs = System.currentTimeMillis(),
                ),
            )
            programRepository.advanceActiveDay()
            progressionRepository.evaluateAfterSession(sessionId)
            analyticsProjectionService.recomputeSession(sessionId)
        }

        private fun SessionSummaryRow.toSummary() =
            SessionSummary(
                id = id,
                startedAtEpochMs = startedAtEpochMs,
                completedAtEpochMs = completedAtEpochMs,
                exerciseCount = exerciseCount,
            )
    }
