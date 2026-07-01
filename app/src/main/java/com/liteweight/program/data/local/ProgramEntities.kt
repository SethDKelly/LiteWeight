package com.liteweight.program.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.liteweight.exercise.data.local.ExerciseKindEntity
import com.liteweight.program.domain.ProgramSourceType

@Entity(
    tableName = "programs",
    indices = [Index(value = ["isArchived"])],
)
data class ProgramEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sourceType: ProgramSourceType,
    val presetId: String?,
    val splitType: String?,
    val progressionSchemeId: Long?,
    val isArchived: Boolean,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)

@Entity(
    tableName = "program_days",
    foreignKeys = [
        ForeignKey(
            entity = ProgramEntity::class,
            parentColumns = ["id"],
            childColumns = ["programId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["programId"])],
)
data class ProgramDayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val programId: Long,
    val dayIndex: Int,
    val name: String,
)

@Entity(
    tableName = "program_exercises",
    foreignKeys = [
        ForeignKey(
            entity = ProgramDayEntity::class,
            parentColumns = ["id"],
            childColumns = ["programDayId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseKindEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseKindId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index(value = ["programDayId"])],
)
data class ProgramExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val programDayId: Long,
    val exerciseKindId: Long,
    val movementSlotId: String?,
    val sortOrder: Int,
    val targetSets: Int,
    val targetRepsMin: Int?,
    val targetRepsMax: Int?,
    val notes: String?,
)

@Entity(tableName = "active_program")
data class ActiveProgramEntity(
    @PrimaryKey val id: Int = 1,
    val programId: Long,
    val rotationPlanId: Long?,
    val currentDayIndex: Int,
    val currentLevelIndex: Int,
    val sessionsAtLevel: Int,
    val activatedAtEpochMs: Long,
)

@Entity(tableName = "installed_presets")
data class InstalledPresetEntity(
    @PrimaryKey val presetId: String,
    val programId: Long,
)
