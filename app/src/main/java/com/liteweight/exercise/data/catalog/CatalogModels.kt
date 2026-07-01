package com.liteweight.exercise.data.catalog

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExerciseCatalogBundle(
    val catalogVersion: Int,
    val sourceAttribution: String,
    val entries: List<CatalogEntryDto>,
)

@Serializable
data class CatalogEntryDto(
    val catalogId: String,
    val displayName: String,
    val namingMode: String,
    val equipment: String? = null,
    val bodyPosition: String? = null,
    val primaryMovement: String? = null,
    val primaryMovementFreeform: String? = null,
    val gripWidth: String? = null,
    val gripOrientation: String? = null,
    val unitType: String,
    val classification: ClassificationDto? = null,
    val instructions: List<String> = emptyList(),
    val comments: List<String> = emptyList(),
    val muscleRoles: List<MuscleRoleDto> = emptyList(),
    val tags: List<String> = emptyList(),
    val referenceUrl: String? = null,
)

@Serializable
data class ClassificationDto(
    val utility: String? = null,
    val mechanics: String? = null,
    val force: String? = null,
    val difficulty: String? = null,
    val exrxCategoryPath: List<String> = emptyList(),
)

@Serializable
data class MuscleRoleDto(
    val muscle: String,
    val role: String,
    val displayName: String? = null,
    val bodyRegion: String? = null,
)
