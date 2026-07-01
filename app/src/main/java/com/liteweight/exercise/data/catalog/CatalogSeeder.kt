package com.liteweight.exercise.data.catalog

import android.content.Context
import com.liteweight.core.data.LiteWeightDatabase
import com.liteweight.exercise.data.local.AppMetadataEntity
import com.liteweight.exercise.data.local.ExerciseClassificationEntity
import com.liteweight.exercise.data.local.ExerciseCommentEntity
import com.liteweight.exercise.data.local.ExerciseInstructionEntity
import com.liteweight.exercise.data.local.ExerciseKindEntity
import com.liteweight.exercise.data.local.ExerciseMuscleRoleEntity
import com.liteweight.exercise.data.local.MuscleVocabularyEntity
import com.liteweight.exercise.domain.BodyPosition
import com.liteweight.exercise.domain.Equipment
import com.liteweight.exercise.domain.GripOrientation
import com.liteweight.exercise.domain.GripWidth
import com.liteweight.exercise.domain.NamingMode
import com.liteweight.exercise.domain.PrimaryMovement
import com.liteweight.exercise.domain.UnitType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import androidx.room.withTransaction

@Singleton
class CatalogSeeder
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val database: LiteWeightDatabase,
    ) {
        private val mutex = Mutex()
        private val json = Json { ignoreUnknownKeys = true }

        suspend fun seedIfNeeded() =
            mutex.withLock {
                val metadataDao = database.appMetadataDao()
                val installedVersion = metadataDao.getValue(KEY_CATALOG_VERSION)?.toIntOrNull() ?: 0
                val bundle = loadBundle()
                if (installedVersion >= bundle.catalogVersion) return@withLock

                database.withTransaction {
                    val exerciseDao = database.exerciseKindDao()
                    val catalogDao = database.exerciseCatalogDao()
                    val now = System.currentTimeMillis()

                    bundle.entries.forEach { entry ->
                        val existing = exerciseDao.getByCatalogId(entry.catalogId)
                        val exerciseId =
                            if (existing != null) {
                                exerciseDao.update(
                                    existing.copy(
                                        displayName = entry.displayName,
                                        namingMode = parseNamingMode(entry.namingMode),
                                        equipment = entry.equipment?.let(::parseEquipment),
                                        bodyPosition = entry.bodyPosition?.let(::parseBodyPosition),
                                        primaryMovement = entry.primaryMovement?.let(::parsePrimaryMovement),
                                        primaryMovementFreeform = entry.primaryMovementFreeform,
                                        gripWidth = entry.gripWidth?.let(::parseGripWidth),
                                        gripOrientation = entry.gripOrientation?.let(::parseGripOrientation),
                                        unitType = parseUnitType(entry.unitType),
                                        muscleTags = entry.tags.joinToString(","),
                                        isBuiltin = true,
                                        isArchived = false,
                                        updatedAtEpochMs = now,
                                    ),
                                )
                                existing.id
                            } else {
                                exerciseDao.insert(
                                    ExerciseKindEntity(
                                        catalogId = entry.catalogId,
                                        displayName = entry.displayName,
                                        namingMode = parseNamingMode(entry.namingMode),
                                        equipment = entry.equipment?.let(::parseEquipment),
                                        bodyPosition = entry.bodyPosition?.let(::parseBodyPosition),
                                        primaryMovement = entry.primaryMovement?.let(::parsePrimaryMovement),
                                        primaryMovementFreeform = entry.primaryMovementFreeform,
                                        gripWidth = entry.gripWidth?.let(::parseGripWidth),
                                        gripOrientation = entry.gripOrientation?.let(::parseGripOrientation),
                                        freeformName = null,
                                        customQualifier = null,
                                        unitType = parseUnitType(entry.unitType),
                                        muscleTags = entry.tags.joinToString(","),
                                        isBuiltin = true,
                                        isArchived = false,
                                        createdAtEpochMs = now,
                                        updatedAtEpochMs = now,
                                    ),
                                )
                            }

                        entry.classification?.let { c ->
                            catalogDao.upsertClassification(
                                ExerciseClassificationEntity(
                                    exerciseKindId = exerciseId,
                                    utility = c.utility,
                                    mechanics = c.mechanics,
                                    force = c.force,
                                    difficulty = c.difficulty,
                                    exrxCategoryPath = c.exrxCategoryPath.joinToString(" > "),
                                ),
                            )
                        }

                        catalogDao.deleteInstructions(exerciseId)
                        catalogDao.upsertInstructions(
                            entry.instructions.mapIndexed { index, text ->
                                ExerciseInstructionEntity(
                                    exerciseKindId = exerciseId,
                                    sortOrder = index,
                                    text = text,
                                )
                            },
                        )

                        catalogDao.deleteComments(exerciseId)
                        catalogDao.upsertComments(
                            entry.comments.mapIndexed { index, text ->
                                ExerciseCommentEntity(
                                    exerciseKindId = exerciseId,
                                    sortOrder = index,
                                    text = text,
                                )
                            },
                        )

                        val muscles =
                            entry.muscleRoles.map { role ->
                                MuscleVocabularyEntity(
                                    muscleSlug = role.muscle,
                                    displayName = role.displayName ?: role.muscle.replace('_', ' '),
                                    bodyRegion = role.bodyRegion,
                                )
                            }
                        catalogDao.insertMuscles(muscles)

                        catalogDao.deleteMuscleRoles(exerciseId)
                        catalogDao.upsertMuscleRoles(
                            entry.muscleRoles.map { role ->
                                ExerciseMuscleRoleEntity(
                                    exerciseKindId = exerciseId,
                                    muscleSlug = role.muscle,
                                    role = role.role.uppercase(),
                                )
                            },
                        )
                    }

                    metadataDao.put(
                        AppMetadataEntity(KEY_CATALOG_VERSION, bundle.catalogVersion.toString()),
                    )
                }
            }

        private fun loadBundle(): ExerciseCatalogBundle {
            val raw =
                context.assets.open(ASSET_PATH).bufferedReader().use { it.readText() }
            return json.decodeFromString(raw)
        }

        companion object {
            const val ASSET_PATH = "catalog/exercise_catalog_v2.json"
            const val KEY_CATALOG_VERSION = "installed_catalog_version"
        }

        private fun parseNamingMode(value: String): NamingMode =
            when (value.lowercase()) {
                "freeform" -> NamingMode.FREEFORM
                else -> NamingMode.STRUCTURED
            }

        private fun parseUnitType(value: String): UnitType = UnitType.valueOf(value.uppercase())

        private fun parseEquipment(value: String): Equipment = Equipment.valueOf(value.uppercase())

        private fun parseBodyPosition(value: String): BodyPosition = BodyPosition.valueOf(value.uppercase())

        private fun parsePrimaryMovement(value: String): PrimaryMovement =
            PrimaryMovement.valueOf(value.uppercase())

        private fun parseGripWidth(value: String): GripWidth = GripWidth.valueOf(value.uppercase())

        private fun parseGripOrientation(value: String): GripOrientation =
            GripOrientation.valueOf(value.uppercase())
    }
