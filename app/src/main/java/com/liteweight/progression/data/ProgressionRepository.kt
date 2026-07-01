package com.liteweight.progression.data

import com.liteweight.core.data.LiteWeightDatabase
import com.liteweight.progression.data.local.ProgressionLevelEntity
import com.liteweight.progression.data.local.ProgressionSchemeEntity
import com.liteweight.progression.domain.ActiveProgressionContext
import com.liteweight.progression.domain.AdvancementRuleType
import com.liteweight.progression.domain.ExercisePrescription
import com.liteweight.progression.domain.ProgressionEvaluator
import com.liteweight.progression.domain.ProgressionLevel
import com.liteweight.progression.domain.ProgressionSchemeDetail
import com.liteweight.progression.domain.ProgressionSchemeSummary
import com.liteweight.progression.domain.SetPerformance
import com.liteweight.session.data.local.SessionStatus
import com.liteweight.program.domain.effectiveProgramId
import com.liteweight.rotation.data.RotationRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class ProgressionRepository
    @Inject
    constructor(
        private val database: LiteWeightDatabase,
        private val rotationRepository: RotationRepository,
    ) {
        private val progressionDao get() = database.progressionDao()
        private val programDao get() = database.programDao()
        private val sessionDao get() = database.workoutSessionDao()

        fun observeSchemes(): Flow<List<ProgressionSchemeSummary>> =
            progressionDao.observeSchemeSummaries().map { rows ->
                rows.map { ProgressionSchemeSummary(it.id, it.name, it.levelCount) }
            }

        suspend fun getSchemeDetail(schemeId: Long): ProgressionSchemeDetail? {
            val scheme = progressionDao.getScheme(schemeId) ?: return null
            val levels = progressionDao.getLevels(schemeId).map { it.toDomain() }
            return ProgressionSchemeDetail(scheme.id, scheme.name, levels)
        }

        suspend fun assignSchemeToProgram(
            programId: Long,
            schemeId: Long?,
        ) {
            programDao.assignProgressionScheme(programId, schemeId, System.currentTimeMillis())
        }

        suspend fun getActiveProgressionContext(): ActiveProgressionContext? {
            val active = programDao.getActiveProgram() ?: return null
            val programId = effectiveProgramId(active, rotationRepository)
            val program = programDao.getProgram(programId) ?: return null
            val schemeId = program.progressionSchemeId ?: return null
            val scheme = progressionDao.getScheme(schemeId) ?: return null
            val levels = progressionDao.getLevels(schemeId)
            if (levels.isEmpty()) return null
            val level = levels.getOrNull(active.currentLevelIndex) ?: levels.first()
            return ActiveProgressionContext(
                schemeId = scheme.id,
                schemeName = scheme.name,
                levelIndex = level.levelIndex,
                levelName = level.name,
                isDeload = level.isDeload,
                advancementRule = level.advancementRule,
                sessionsAtLevel = active.sessionsAtLevel,
                sessionsRequired = level.sessionsRequired,
                loadIncrement = level.loadIncrement,
                targetRepsMin = level.targetRepsMin,
                targetRepsMax = level.targetRepsMax,
            )
        }

        suspend fun getProgramSchemeId(programId: Long): Long? =
            programDao.getProgram(programId)?.progressionSchemeId

        suspend fun evaluateAfterSession(sessionId: Long) {
            val active = programDao.getActiveProgram() ?: return
            val programId = effectiveProgramId(active, rotationRepository)
            val program = programDao.getProgram(programId) ?: return
            val schemeId = program.progressionSchemeId ?: return
            val levels = progressionDao.getLevels(schemeId)
            if (levels.isEmpty()) return

            val currentLevel = levels.getOrNull(active.currentLevelIndex) ?: return
            val session = sessionDao.getSession(sessionId) ?: return
            if (session.status != SessionStatus.COMPLETED) return

            val days = programDao.getDays(programId)
            val day = days.firstOrNull { it.dayIndex == active.currentDayIndex } ?: return
            val prescriptions =
                programDao.getExercisesForDay(day.id).map { row ->
                    ExercisePrescription(
                        exerciseKindId = row.exerciseKindId,
                        targetSets = row.targetSets,
                        targetRepsMin = row.targetRepsMin,
                    )
                }

            val entries = sessionDao.getSessionExercises(sessionId)
            val completedSets = mutableListOf<SetPerformance>()
            entries.forEach { entry ->
                sessionDao.getSets(entry.entryId).forEach { set ->
                    completedSets +=
                        SetPerformance(
                            exerciseKindId = entry.exerciseKindId,
                            substitutedFromExerciseKindId = entry.substitutedFromExerciseKindId,
                            repCount = set.repCount,
                            isWarmup = set.isWarmup,
                            isCompleted = set.isCompleted,
                        )
                }
            }

            val nextSessionsAtLevel =
                if (currentLevel.advancementRule == AdvancementRuleType.SESSION_COUNT) {
                    active.sessionsAtLevel + 1
                } else {
                    active.sessionsAtLevel
                }

            val shouldAdvance =
                ProgressionEvaluator.evaluateAdvancement(
                    rule = currentLevel.advancementRule,
                    sessionsAtLevel = nextSessionsAtLevel,
                    sessionsRequired = currentLevel.sessionsRequired,
                    completedSets = completedSets,
                    prescriptions = prescriptions,
                    levelRepMin = currentLevel.targetRepsMin,
                )

            if (shouldAdvance) {
                val nextLevel = ProgressionEvaluator.nextLevelIndex(active.currentLevelIndex, levels.size)
                programDao.updateActiveLevel(nextLevel, sessionsAtLevel = 0)
            } else if (currentLevel.advancementRule == AdvancementRuleType.SESSION_COUNT) {
                programDao.updateActiveLevel(active.currentLevelIndex, sessionsAtLevel = nextSessionsAtLevel)
            }
        }

        suspend fun manualAdvanceLevel() {
            val active = programDao.getActiveProgram() ?: return
            val program = programDao.getProgram(active.programId) ?: return
            val schemeId = program.progressionSchemeId ?: return
            val levels = progressionDao.getLevels(schemeId)
            if (levels.isEmpty()) return
            val nextLevel = ProgressionEvaluator.nextLevelIndex(active.currentLevelIndex, levels.size)
            programDao.updateActiveLevel(nextLevel, sessionsAtLevel = 0)
        }

        suspend fun createBuiltinSchemeIfEmpty(): Long? {
            if (progressionDao.schemeCount() > 0) return null
            return insertLinearScheme()
        }

        private suspend fun insertLinearScheme(): Long {
            val now = System.currentTimeMillis()
            val schemeId =
                progressionDao.insertScheme(
                    ProgressionSchemeEntity(
                        name = "Linear 3-phase",
                        isBuiltin = true,
                        createdAtEpochMs = now,
                    ),
                )
            listOf(
                ProgressionLevelEntity(
                    schemeId = schemeId,
                    levelIndex = 0,
                    name = "Base",
                    loadIncrement = 0.0,
                    targetRepsMin = 10,
                    targetRepsMax = 12,
                    sessionsRequired = 3,
                    advancementRule = AdvancementRuleType.SESSION_COUNT,
                    isDeload = false,
                ),
                ProgressionLevelEntity(
                    schemeId = schemeId,
                    levelIndex = 1,
                    name = "Build",
                    loadIncrement = 2.5,
                    targetRepsMin = 8,
                    targetRepsMax = 10,
                    sessionsRequired = 3,
                    advancementRule = AdvancementRuleType.REP_TARGET,
                    isDeload = false,
                ),
                ProgressionLevelEntity(
                    schemeId = schemeId,
                    levelIndex = 2,
                    name = "Peak",
                    loadIncrement = 2.5,
                    targetRepsMin = 5,
                    targetRepsMax = 8,
                    sessionsRequired = null,
                    advancementRule = AdvancementRuleType.MANUAL_ONLY,
                    isDeload = false,
                ),
                ProgressionLevelEntity(
                    schemeId = schemeId,
                    levelIndex = 3,
                    name = "Deload",
                    loadIncrement = 0.0,
                    targetRepsMin = 10,
                    targetRepsMax = 12,
                    sessionsRequired = 2,
                    advancementRule = AdvancementRuleType.SESSION_COUNT,
                    isDeload = true,
                ),
            ).forEach { progressionDao.insertLevel(it) }
            return schemeId
        }

        private fun ProgressionLevelEntity.toDomain() =
            ProgressionLevel(
                id = id,
                levelIndex = levelIndex,
                name = name,
                loadIncrement = loadIncrement,
                targetRepsMin = targetRepsMin,
                targetRepsMax = targetRepsMax,
                sessionsRequired = sessionsRequired,
                advancementRule = advancementRule,
                isDeload = isDeload,
            )
    }
