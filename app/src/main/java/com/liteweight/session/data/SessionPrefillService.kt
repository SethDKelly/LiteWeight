package com.liteweight.session.data

import com.liteweight.core.data.LiteWeightDatabase
import com.liteweight.program.domain.PrefillMode
import com.liteweight.program.domain.ProgramExerciseDetail
import com.liteweight.progression.data.ProgressionRepository
import com.liteweight.progression.domain.ProgressionEvaluator
import com.liteweight.session.data.local.WorkoutExerciseEntryEntity
import com.liteweight.session.data.local.WorkoutSetEntryEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionPrefillService
    @Inject
    constructor(
        private val database: LiteWeightDatabase,
        private val progressionRepository: ProgressionRepository,
    ) {
        private val sessionDao get() = database.workoutSessionDao()

        suspend fun prefillSession(
            sessionId: Long,
            exercises: List<ProgramExerciseDetail>,
            mode: PrefillMode,
        ) {
            val progression = progressionRepository.getActiveProgressionContext()
            exercises.forEachIndexed { index, prescription ->
                val entryId =
                    sessionDao.insertExerciseEntry(
                        WorkoutExerciseEntryEntity(
                            sessionId = sessionId,
                            exerciseKindId = prescription.exerciseKindId,
                            movementSlotId = prescription.movementSlotId,
                            sortOrder = index,
                            substitutedFromExerciseKindId = null,
                        ),
                    )
                when (mode) {
                    PrefillMode.BLANK -> Unit
                    PrefillMode.PRESCRIPTION_ONLY,
                    PrefillMode.CARRY_LAST_SUCCESS,
                    -> {
                        val rawLastLoad =
                            if (mode == PrefillMode.CARRY_LAST_SUCCESS) {
                                sessionDao.getLastCompletedSetsForExercise(prescription.exerciseKindId, 1)
                                    .firstOrNull()
                                    ?.loadValue
                            } else {
                                null
                            }
                        val lastLoad =
                            if (mode == PrefillMode.CARRY_LAST_SUCCESS && progression != null && rawLastLoad != null) {
                                ProgressionEvaluator.adjustedLoad(
                                    lastLoad = rawLastLoad,
                                    loadIncrement = progression.loadIncrement,
                                    isDeload = progression.isDeload,
                                )
                            } else {
                                rawLastLoad
                            }
                        val repTarget =
                            progression?.targetRepsMax
                                ?: progression?.targetRepsMin
                                ?: prescription.targetRepsMax
                                ?: prescription.targetRepsMin
                        repeat(prescription.targetSets) { setIndex ->
                            sessionDao.insertSet(
                                WorkoutSetEntryEntity(
                                    workoutExerciseEntryId = entryId,
                                    setIndex = setIndex,
                                    loadValue = lastLoad,
                                    repCount = repTarget,
                                    isWarmup = false,
                                    isCompleted = false,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
