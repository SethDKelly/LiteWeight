package com.liteweight.exercise.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.liteweight.exercise.domain.BodyPosition
import com.liteweight.exercise.domain.Equipment
import com.liteweight.exercise.domain.GripOrientation
import com.liteweight.exercise.domain.GripWidth
import com.liteweight.exercise.domain.NamingMode
import com.liteweight.exercise.domain.PrimaryMovement
import com.liteweight.exercise.domain.UnitType

@Entity(
    tableName = "exercise_kinds",
    indices = [
        Index(value = ["displayName"]),
        Index(value = ["isArchived"]),
        Index(value = ["catalogId"], unique = true),
    ],
)
data class ExerciseKindEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val catalogId: String?,
    val displayName: String,
    val namingMode: NamingMode,
    val equipment: Equipment?,
    val bodyPosition: BodyPosition?,
    val primaryMovement: PrimaryMovement?,
    val primaryMovementFreeform: String?,
    val gripWidth: GripWidth?,
    val gripOrientation: GripOrientation?,
    val freeformName: String?,
    val customQualifier: String?,
    val unitType: UnitType,
    val muscleTags: String?,
    val isBuiltin: Boolean,
    val isArchived: Boolean,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)
