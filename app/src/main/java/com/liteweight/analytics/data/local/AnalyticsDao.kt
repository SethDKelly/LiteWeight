package com.liteweight.analytics.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.liteweight.analytics.domain.PrType
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalyticsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSnapshot(entity: ExerciseSessionSnapshotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSummary(entity: ExerciseAnalyticsSummaryEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPrEvent(entity: ExercisePrEventEntity)

    @Query(
        """
        SELECT * FROM exercise_session_snapshots
        WHERE exerciseKindId = :exerciseKindId
        AND (:sinceMs IS NULL OR completedAtEpochMs >= :sinceMs)
        ORDER BY completedAtEpochMs ASC
        """,
    )
    suspend fun getSnapshots(
        exerciseKindId: Long,
        sinceMs: Long?,
    ): List<ExerciseSessionSnapshotEntity>

    @Query("SELECT * FROM exercise_analytics_summaries WHERE exerciseKindId = :exerciseKindId")
    suspend fun getSummary(exerciseKindId: Long): ExerciseAnalyticsSummaryEntity?

    @Query("SELECT * FROM exercise_pr_events WHERE exerciseKindId = :exerciseKindId ORDER BY achievedAtEpochMs DESC")
    fun observePrTimeline(exerciseKindId: Long): Flow<List<ExercisePrEventEntity>>

    @Query("SELECT * FROM exercise_pr_events WHERE exerciseKindId = :exerciseKindId ORDER BY achievedAtEpochMs DESC")
    suspend fun getPrTimeline(exerciseKindId: Long): List<ExercisePrEventEntity>

    @Query(
        """
        SELECT * FROM exercise_pr_events
        WHERE exerciseKindId = :exerciseKindId AND prType = :prType
        ORDER BY value DESC, achievedAtEpochMs DESC
        LIMIT 1
        """,
    )
    suspend fun getBestPr(
        exerciseKindId: Long,
        prType: PrType,
    ): ExercisePrEventEntity?

    @Query("SELECT COUNT(*) FROM exercise_session_snapshots WHERE exerciseKindId = :exerciseKindId")
    suspend fun countSnapshots(exerciseKindId: Long): Int

    @Query(
        """
        SELECT MAX(completedAtEpochMs) FROM exercise_session_snapshots
        WHERE exerciseKindId = :exerciseKindId
        """,
    )
    suspend fun lastSnapshotAt(exerciseKindId: Long): Long?

    @Query(
        """
        SELECT sessionId FROM exercise_pr_events
        WHERE exerciseKindId = :exerciseKindId AND prType = :prType
        """,
    )
    suspend fun getPrSessionIds(
        exerciseKindId: Long,
        prType: PrType,
    ): List<Long>
}
