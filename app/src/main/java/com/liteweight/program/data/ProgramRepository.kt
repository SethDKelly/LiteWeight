package com.liteweight.program.data

import android.content.Context
import com.liteweight.core.data.LiteWeightDatabase
import com.liteweight.exercise.data.local.ExerciseKindEntity
import com.liteweight.program.data.catalog.PresetProgramBundle
import com.liteweight.program.data.catalog.PresetProgramDto
import com.liteweight.program.data.local.ActiveProgramEntity
import com.liteweight.program.data.local.InstalledPresetEntity
import com.liteweight.program.data.local.ProgramDayEntity
import com.liteweight.program.data.local.ProgramEntity
import com.liteweight.program.data.local.ProgramExerciseEntity
import com.liteweight.program.domain.ActiveProgramContext
import com.liteweight.program.domain.PresetProgramSummary
import com.liteweight.program.domain.ProgramDayDetail
import com.liteweight.program.domain.ProgramDetail
import com.liteweight.program.domain.ProgramExerciseDetail
import com.liteweight.program.domain.ProgramSourceType
import com.liteweight.program.domain.ProgramSummary
import com.liteweight.program.domain.effectiveProgramId
import com.liteweight.progression.data.ProgressionRepository
import com.liteweight.rotation.data.RotationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

@Singleton
class ProgramRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val database: LiteWeightDatabase,
        private val progressionRepository: ProgressionRepository,
        private val rotationRepository: RotationRepository,
    ) {
        private val programDao get() = database.programDao()
        private val exerciseDao get() = database.exerciseKindDao()
        private val json = Json { ignoreUnknownKeys = true }

        fun observePrograms(): Flow<List<ProgramSummary>> =
            programDao.observeProgramSummaries().map { rows ->
                rows.map {
                    ProgramSummary(
                        id = it.id,
                        name = it.name,
                        sourceType = it.sourceType,
                        splitType = it.splitType,
                        dayCount = it.dayCount,
                        isActive = it.isActive,
                    )
                }
            }

        suspend fun getProgramDetail(programId: Long): ProgramDetail? {
            val program = programDao.getProgram(programId) ?: return null
            val days =
                programDao.getDays(programId).map { day ->
                    val exercises =
                        programDao.getExercisesForDay(day.id).map { row ->
                            ProgramExerciseDetail(
                                id = row.id,
                                exerciseKindId = row.exerciseKindId,
                                displayName = row.displayName,
                                movementSlotId = row.movementSlotId,
                                movementSlotLabel = row.movementSlotLabel,
                                targetSets = row.targetSets,
                                targetRepsMin = row.targetRepsMin,
                                targetRepsMax = row.targetRepsMax,
                                notes = row.notes,
                            )
                        }
                    ProgramDayDetail(day.id, day.dayIndex, day.name, exercises)
                }
            return ProgramDetail(
                program.id,
                program.name,
                program.sourceType,
                program.splitType,
                program.progressionSchemeId,
                days,
            )
        }

        suspend fun createCustomProgram(name: String, splitType: String?): Long {
            val now = System.currentTimeMillis()
            return programDao.insertProgram(
                ProgramEntity(
                    name = name.trim(),
                    sourceType = ProgramSourceType.CUSTOM,
                    presetId = null,
                    splitType = splitType,
                    progressionSchemeId = null,
                    isArchived = false,
                    createdAtEpochMs = now,
                    updatedAtEpochMs = now,
                ),
            )
        }

        suspend fun addDay(programId: Long, name: String): Long {
            val dayIndex = programDao.maxDayIndex(programId) + 1
            return programDao.insertDay(
                ProgramDayEntity(
                    programId = programId,
                    dayIndex = dayIndex,
                    name = name.trim(),
                ),
            )
        }

        suspend fun addExerciseToDay(
            programDayId: Long,
            exerciseKindId: Long,
            targetSets: Int,
            targetRepsMin: Int?,
            targetRepsMax: Int?,
        ) {
            val sortOrder = programDao.maxExerciseSortOrder(programDayId) + 1
            programDao.insertExercise(
                ProgramExerciseEntity(
                    programDayId = programDayId,
                    exerciseKindId = exerciseKindId,
                    movementSlotId = null,
                    sortOrder = sortOrder,
                    targetSets = targetSets,
                    targetRepsMin = targetRepsMin,
                    targetRepsMax = targetRepsMax,
                    notes = null,
                ),
            )
        }

        suspend fun substituteProgramExercise(
            programExerciseId: Long,
            exerciseKindId: Long,
        ) {
            programDao.updateProgramExerciseKind(programExerciseId, exerciseKindId)
        }

        suspend fun activateProgram(programId: Long, dayIndex: Int = 0) {
            programDao.setActiveProgram(
                ActiveProgramEntity(
                    programId = programId,
                    rotationPlanId = null,
                    currentDayIndex = dayIndex,
                    currentLevelIndex = 0,
                    sessionsAtLevel = 0,
                    activatedAtEpochMs = System.currentTimeMillis(),
                ),
            )
        }

        fun observeActiveProgram(): Flow<ActiveProgramEntity?> = programDao.observeActiveProgram()

        suspend fun getActiveContext(): ActiveProgramContext? {
            val active = programDao.getActiveProgram() ?: return null
            val rotation = rotationRepository.resolveActive()
            val programId = effectiveProgramId(active, rotationRepository)
            val program = programDao.getProgram(programId) ?: return null
            val days = programDao.getDays(programId)
            val day = days.firstOrNull { it.dayIndex == active.currentDayIndex } ?: days.firstOrNull() ?: return null
            val exerciseCount = programDao.getExercisesForDay(day.id).size
            return ActiveProgramContext(
                programId = program.id,
                programName = program.name,
                dayIndex = day.dayIndex,
                dayName = day.name,
                exerciseCount = exerciseCount,
                progression = progressionRepository.getActiveProgressionContext(),
                rotation = rotation,
            )
        }

        suspend fun advanceActiveDay() {
            val active = programDao.getActiveProgram() ?: return
            val programId = effectiveProgramId(active, rotationRepository)
            val days = programDao.getDays(programId)
            if (days.isEmpty()) return
            val nextIndex = (active.currentDayIndex + 1) % days.size
            programDao.updateActiveDayIndex(nextIndex)
        }

        suspend fun listPresets(): List<PresetProgramSummary> {
            val bundle = loadPresetBundle()
            return bundle.presets.map { preset ->
                val installedId = programDao.getInstalledProgramId(preset.presetId)
                PresetProgramSummary(
                    presetId = preset.presetId,
                    name = preset.name,
                    difficulty = preset.difficulty,
                    splitType = preset.splitType,
                    dayCount = preset.days.size,
                    isInstalled = installedId != null,
                )
            }
        }

        suspend fun installPreset(presetId: String): Long {
            val existing = programDao.getInstalledProgramId(presetId)
            if (existing != null) return existing
            val preset = loadPresetBundle().presets.first { it.presetId == presetId }
            val programId = insertProgramFromPreset(preset)
            programDao.markPresetInstalled(InstalledPresetEntity(presetId, programId))
            return programId
        }

        suspend fun getExercisesForActiveDay(): List<ProgramExerciseDetail> {
            val active = programDao.getActiveProgram() ?: return emptyList()
            val programId = effectiveProgramId(active, rotationRepository)
            val days = programDao.getDays(programId)
            val day = days.firstOrNull { it.dayIndex == active.currentDayIndex } ?: return emptyList()
            return programDao.getExercisesForDay(day.id).map { row ->
                ProgramExerciseDetail(
                    id = row.id,
                    exerciseKindId = row.exerciseKindId,
                    displayName = row.displayName,
                    movementSlotId = row.movementSlotId,
                    movementSlotLabel = row.movementSlotLabel,
                    targetSets = row.targetSets,
                    targetRepsMin = row.targetRepsMin,
                    targetRepsMax = row.targetRepsMax,
                    notes = row.notes,
                )
            }
        }

        private suspend fun insertProgramFromPreset(preset: PresetProgramDto): Long {
            val now = System.currentTimeMillis()
            val programId =
                programDao.insertProgram(
                    ProgramEntity(
                        name = preset.name,
                        sourceType = ProgramSourceType.PRESET_INSTALLED,
                        presetId = preset.presetId,
                        splitType = preset.splitType,
                        progressionSchemeId = null,
                        isArchived = false,
                        createdAtEpochMs = now,
                        updatedAtEpochMs = now,
                    ),
                )
            preset.days.forEachIndexed { index, day ->
                val dayId =
                    programDao.insertDay(
                        ProgramDayEntity(
                            programId = programId,
                            dayIndex = index,
                            name = day.name,
                        ),
                    )
                day.exercises.forEachIndexed { exerciseIndex, exercise ->
                    val exerciseKindId =
                        exerciseDao.getByCatalogId(exercise.catalogId)?.id
                            ?: error("Missing catalog exercise ${exercise.catalogId}")
                    programDao.insertExercise(
                        ProgramExerciseEntity(
                            programDayId = dayId,
                            exerciseKindId = exerciseKindId,
                            movementSlotId = null,
                            sortOrder = exerciseIndex,
                            targetSets = exercise.targetSets,
                            targetRepsMin = exercise.targetRepsMin,
                            targetRepsMax = exercise.targetRepsMax,
                            notes = exercise.notes,
                        ),
                    )
                }
            }
            return programId
        }

        private fun loadPresetBundle(): PresetProgramBundle {
            val raw = context.assets.open(ASSET_PATH).bufferedReader().use { it.readText() }
            return json.decodeFromString(raw)
        }

        companion object {
            const val ASSET_PATH = "catalog/preset_programs_v1.json"
        }
    }
