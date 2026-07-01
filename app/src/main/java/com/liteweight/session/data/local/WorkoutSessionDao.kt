package com.liteweight.session.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class SessionExerciseWithName(
    val entryId: Long,
    val exerciseKindId: Long,
    val displayName: String,
    val sortOrder: Int,
    val movementSlotId: String?,
    val substitutedFromExerciseKindId: Long?,
)

data class SessionSummaryRow(
    val id: Long,
    val status: SessionStatus,
    val startedAtEpochMs: Long,
    val completedAtEpochMs: Long?,
    val exerciseCount: Int,
)

@Dao
interface WorkoutSessionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSession(entity: WorkoutSessionEntity): Long

    @Update
    suspend fun updateSession(entity: WorkoutSessionEntity)

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getSession(id: Long): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE status = 'DRAFT' ORDER BY startedAtEpochMs DESC LIMIT 1")
    suspend fun getLatestDraft(): WorkoutSessionEntity?

    @Query(
        """
        SELECT s.id, s.status, s.startedAtEpochMs, s.completedAtEpochMs,
               (SELECT COUNT(*) FROM workout_exercise_entries e WHERE e.sessionId = s.id) AS exerciseCount
        FROM workout_sessions s
        WHERE s.status = 'COMPLETED'
        ORDER BY s.completedAtEpochMs DESC
        """,
    )
    fun observeCompletedSummaries(): Flow<List<SessionSummaryRow>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExerciseEntry(entity: WorkoutExerciseEntryEntity): Long

    @Update
    suspend fun updateExerciseEntry(entity: WorkoutExerciseEntryEntity)

    @Query("SELECT * FROM workout_exercise_entries WHERE id = :id")
    suspend fun getExerciseEntry(id: Long): WorkoutExerciseEntryEntity?

    @Query(
        """
        SELECT e.id AS entryId, e.exerciseKindId, k.displayName, e.sortOrder,
               e.movementSlotId, e.substitutedFromExerciseKindId
        FROM workout_exercise_entries e
        INNER JOIN exercise_kinds k ON k.id = e.exerciseKindId
        WHERE e.sessionId = :sessionId
        ORDER BY e.sortOrder
        """,
    )
    fun observeSessionExercises(sessionId: Long): Flow<List<SessionExerciseWithName>>

    @Query(
        """
        SELECT e.id AS entryId, e.exerciseKindId, k.displayName, e.sortOrder,
               e.movementSlotId, e.substitutedFromExerciseKindId
        FROM workout_exercise_entries e
        INNER JOIN exercise_kinds k ON k.id = e.exerciseKindId
        WHERE e.sessionId = :sessionId
        ORDER BY e.sortOrder
        """,
    )
    suspend fun getSessionExercises(sessionId: Long): List<SessionExerciseWithName>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSet(entity: WorkoutSetEntryEntity): Long

    @Update
    suspend fun updateSet(entity: WorkoutSetEntryEntity)

    @Query("SELECT * FROM workout_set_entries WHERE workoutExerciseEntryId = :entryId ORDER BY setIndex")
    fun observeSets(entryId: Long): Flow<List<WorkoutSetEntryEntity>>

    @Query("SELECT * FROM workout_set_entries WHERE workoutExerciseEntryId = :entryId ORDER BY setIndex")
    suspend fun getSets(entryId: Long): List<WorkoutSetEntryEntity>

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM workout_exercise_entries WHERE sessionId = :sessionId")
    suspend fun maxExerciseSortOrder(sessionId: Long): Int

    @Query("SELECT COALESCE(MAX(setIndex), -1) FROM workout_set_entries WHERE workoutExerciseEntryId = :entryId")
    suspend fun maxSetIndex(entryId: Long): Int

    @Query(
        """
        SELECT s.* FROM workout_set_entries s
        INNER JOIN workout_exercise_entries e ON e.id = s.workoutExerciseEntryId
        INNER JOIN workout_sessions ws ON ws.id = e.sessionId
        WHERE e.exerciseKindId = :exerciseKindId
        AND ws.status = 'COMPLETED'
        AND s.isCompleted = 1
        ORDER BY ws.completedAtEpochMs DESC, s.setIndex DESC
        LIMIT :limit
        """,
    )
    suspend fun getLastCompletedSetsForExercise(exerciseKindId: Long, limit: Int): List<WorkoutSetEntryEntity>

    @Query(
        """
        SELECT COUNT(*) FROM workout_sessions
        WHERE status = 'COMPLETED' AND completedAtEpochMs >= :sinceMs
        """,
    )
    suspend fun countCompletedSince(sinceMs: Long): Int
}
