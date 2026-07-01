package com.liteweight.generator.data

import com.liteweight.core.data.LiteWeightDatabase
import com.liteweight.generator.domain.GeneratorInput
import com.liteweight.generator.domain.ProgramDraft
import com.liteweight.generator.domain.RoutineGeneratorEngine
import com.liteweight.program.data.local.ProgramDayEntity
import com.liteweight.program.data.local.ProgramEntity
import com.liteweight.program.data.local.ProgramExerciseEntity
import com.liteweight.program.domain.ProgramSourceType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineGeneratorRepository
    @Inject
    constructor(
        private val database: LiteWeightDatabase,
    ) {
        private val programDao get() = database.programDao()
        private val exerciseDao get() = database.exerciseKindDao()

        fun generateDraft(input: GeneratorInput): ProgramDraft = RoutineGeneratorEngine.generate(input)

        suspend fun acceptDraft(draft: ProgramDraft): Long {
            val now = System.currentTimeMillis()
            val programId =
                programDao.insertProgram(
                    ProgramEntity(
                        name = draft.name,
                        sourceType = ProgramSourceType.CUSTOM,
                        presetId = null,
                        splitType = draft.splitType,
                        progressionSchemeId = null,
                        isArchived = false,
                        createdAtEpochMs = now,
                        updatedAtEpochMs = now,
                    ),
                )
            draft.days.forEachIndexed { index, day ->
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
                            movementSlotId = exercise.movementSlotId,
                            sortOrder = exerciseIndex,
                            targetSets = exercise.targetSets,
                            targetRepsMin = exercise.targetRepsMin,
                            targetRepsMax = exercise.targetRepsMax,
                            notes = null,
                        ),
                    )
                }
            }
            return programId
        }
    }
