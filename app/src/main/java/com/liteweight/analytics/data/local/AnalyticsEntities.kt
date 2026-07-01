package com.liteweight.analytics.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.liteweight.analytics.domain.PrType
import com.liteweight.exercise.data.local.ExerciseKindEntity
import com.liteweight.session.data.local.WorkoutSessionEntity

@Entity(
    tableName = "exercise_session_snapshots",
    primaryKeys = ["exerciseKindId", "sessionId"],
    foreignKeys = [
        ForeignKey(
            entity = ExerciseKindEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseKindId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["exerciseKindId", "completedAtEpochMs"])],
)
data class ExerciseSessionSnapshotEntity(
    val exerciseKindId: Long,
    val sessionId: Long,
    val completedAtEpochMs: Long,
    val topSetLoad: Double?,
    val sessionVolume: Double?,
    val estimatedOneRm: Double?,
    val topSetReps: Int?,
)

@Entity(
    tableName = "exercise_pr_events",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseKindEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseKindId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["exerciseKindId", "achievedAtEpochMs"])],
)
data class ExercisePrEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseKindId: Long,
    val prType: PrType,
    val value: Double,
    val sessionId: Long,
    val setId: Long?,
    val achievedAtEpochMs: Long,
)

@Entity(
    tableName = "exercise_analytics_summaries",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseKindEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseKindId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class ExerciseAnalyticsSummaryEntity(
    @PrimaryKey val exerciseKindId: Long,
    val sessionCount: Int,
    val lastSessionAtEpochMs: Long?,
    val bestLoad: Double?,
    val bestLoadAtEpochMs: Long?,
    val bestVolume: Double?,
    val bestE1rm: Double?,
    val bestReps: Int?,
)
