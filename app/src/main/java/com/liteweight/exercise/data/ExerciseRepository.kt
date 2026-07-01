package com.liteweight.exercise.data

import com.liteweight.core.data.LiteWeightDatabase
import com.liteweight.exercise.data.local.ExerciseKindEntity
import com.liteweight.exercise.data.local.MuscleFilterOption
import com.liteweight.exercise.domain.CreateStructuredExerciseInput
import com.liteweight.exercise.domain.ExerciseDetail
import com.liteweight.exercise.domain.ExerciseDisplayNameBuilder
import com.liteweight.exercise.domain.ExerciseFilters
import com.liteweight.exercise.domain.ExerciseSummary
import com.liteweight.exercise.domain.NamingMode
import com.liteweight.exercise.domain.toSummary
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class ExerciseRepository
    @Inject
    constructor(
        private val database: LiteWeightDatabase,
    ) {
        private val exerciseDao get() = database.exerciseKindDao()
        private val catalogDao get() = database.exerciseCatalogDao()

        fun observeTargetMuscles(): Flow<List<MuscleFilterOption>> = exerciseDao.observeTargetMuscles()

        fun observeExercises(filters: ExerciseFilters): Flow<List<ExerciseSummary>> =
            exerciseDao
                .observeFiltered(
                    query = filters.query.trim(),
                    equipment = filters.equipment,
                    mechanics = filters.mechanics,
                    force = filters.force,
                    muscleSlug = filters.muscleSlug,
                ).map { list -> list.map { it.toSummary() } }

        fun observeAllExercises(): Flow<List<ExerciseSummary>> =
            exerciseDao.observeAllActive().map { list -> list.map { it.toSummary() } }

        suspend fun suggestSubstitutes(exerciseKindId: Long): List<ExerciseSummary> =
            exerciseDao.findSubstituteCandidates(exerciseKindId).map { it.toSummary() }

        suspend fun getDetail(id: Long): ExerciseDetail? {
            val exercise = exerciseDao.getById(id) ?: return null
            val classification = catalogDao.getClassification(id)
            val classificationText =
                classification?.let { c ->
                    listOfNotNull(c.mechanics, c.force, c.difficulty)
                        .joinToString(" · ")
                        .takeIf { it.isNotEmpty() }
                }
            return ExerciseDetail(
                id = exercise.id,
                displayName = exercise.displayName,
                isBuiltin = exercise.isBuiltin,
                unitType = exercise.unitType,
                classification = classificationText,
                instructions = catalogDao.getInstructions(id).map { it.text },
                comments = catalogDao.getComments(id).map { it.text },
                muscles = catalogDao.getMuscleRoles(id).map { it.displayName to it.role },
            )
        }

        suspend fun createStructured(input: CreateStructuredExerciseInput): Long {
            val now = System.currentTimeMillis()
            val displayName =
                ExerciseDisplayNameBuilder.build(
                    equipment = input.equipment,
                    bodyPosition = input.bodyPosition,
                    primaryMovement = input.primaryMovement,
                    primaryMovementFreeform = input.primaryMovementFreeform,
                    gripWidth = input.gripWidth,
                    gripOrientation = input.gripOrientation,
                    customDisplayName = input.customDisplayName,
                )
            return exerciseDao.insert(
                ExerciseKindEntity(
                    catalogId = null,
                    displayName = displayName,
                    namingMode = NamingMode.STRUCTURED,
                    equipment = input.equipment,
                    bodyPosition = input.bodyPosition,
                    primaryMovement = input.primaryMovement,
                    primaryMovementFreeform = input.primaryMovementFreeform,
                    gripWidth = input.gripWidth,
                    gripOrientation = input.gripOrientation,
                    freeformName = null,
                    customQualifier = null,
                    unitType = input.unitType,
                    muscleTags = null,
                    isBuiltin = false,
                    isArchived = false,
                    createdAtEpochMs = now,
                    updatedAtEpochMs = now,
                ),
            )
        }

        suspend fun createFreeform(name: String): Long {
            val now = System.currentTimeMillis()
            val trimmed = name.trim()
            require(trimmed.isNotEmpty())
            return exerciseDao.insert(
                ExerciseKindEntity(
                    catalogId = null,
                    displayName = trimmed,
                    namingMode = NamingMode.FREEFORM,
                    equipment = null,
                    bodyPosition = null,
                    primaryMovement = null,
                    primaryMovementFreeform = null,
                    gripWidth = null,
                    gripOrientation = null,
                    freeformName = trimmed,
                    customQualifier = null,
                    unitType = com.liteweight.exercise.domain.UnitType.WEIGHT,
                    muscleTags = null,
                    isBuiltin = false,
                    isArchived = false,
                    createdAtEpochMs = now,
                    updatedAtEpochMs = now,
                ),
            )
        }

        suspend fun archive(id: Long) {
            exerciseDao.archive(id, System.currentTimeMillis())
        }
    }
