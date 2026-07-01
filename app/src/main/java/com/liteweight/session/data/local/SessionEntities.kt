package com.liteweight.session.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.liteweight.exercise.data.local.ExerciseKindEntity

enum class SessionStatus {
    DRAFT,
    COMPLETED,
    DISCARDED,
}

@Entity(
    tableName = "workout_sessions",
    indices = [Index(value = ["status"]), Index(value = ["completedAtEpochMs"])],
)
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val status: SessionStatus,
    val startedAtEpochMs: Long,
    val completedAtEpochMs: Long?,
    val notes: String?,
)

@Entity(
    tableName = "workout_exercise_entries",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseKindEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseKindId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index(value = ["sessionId"])],
)
data class WorkoutExerciseEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseKindId: Long,
    val movementSlotId: String?,
    val sortOrder: Int,
    val substitutedFromExerciseKindId: Long?,
)

@Entity(
    tableName = "workout_set_entries",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutExerciseEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutExerciseEntryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["workoutExerciseEntryId"])],
)
data class WorkoutSetEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutExerciseEntryId: Long,
    val setIndex: Int,
    val loadValue: Double?,
    val repCount: Int?,
    val isWarmup: Boolean,
    val isCompleted: Boolean,
)
