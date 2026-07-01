package com.liteweight.exercise.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.liteweight.exercise.domain.Equipment
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseKindDao {
    @Query(
        """
        SELECT e.* FROM exercise_kinds e
        LEFT JOIN exercise_classifications c ON c.exerciseKindId = e.id
        WHERE e.isArchived = 0
        AND e.displayName LIKE '%' || :query || '%'
        AND (:equipment IS NULL OR e.equipment = :equipment)
        AND (:mechanics IS NULL OR c.mechanics = :mechanics)
        AND (:force IS NULL OR c.force = :force)
        AND (
            :muscleSlug IS NULL OR EXISTS (
                SELECT 1 FROM exercise_muscle_roles r
                WHERE r.exerciseKindId = e.id
                AND r.muscleSlug = :muscleSlug
                AND r.role = 'TARGET'
            )
        )
        GROUP BY e.id
        ORDER BY e.isBuiltin DESC, e.displayName ASC
        """,
    )
    fun observeFiltered(
        query: String,
        equipment: Equipment?,
        mechanics: String?,
        force: String?,
        muscleSlug: String?,
    ): Flow<List<ExerciseKindEntity>>

    @Query(
        """
        SELECT DISTINCT e.* FROM exercise_kinds e
        INNER JOIN exercise_muscle_roles r1 ON r1.exerciseKindId = e.id AND r1.role = 'TARGET'
        INNER JOIN exercise_muscle_roles r2 ON r2.muscleSlug = r1.muscleSlug AND r2.exerciseKindId = :exerciseKindId AND r2.role = 'TARGET'
        WHERE e.isArchived = 0
        AND e.id != :exerciseKindId
        ORDER BY e.isBuiltin DESC, e.displayName ASC
        LIMIT :limit
        """,
    )
    suspend fun findSubstituteCandidates(exerciseKindId: Long, limit: Int = 8): List<ExerciseKindEntity>

    @Query(
        """
        SELECT DISTINCT m.muscleSlug, m.displayName
        FROM muscle_vocabulary m
        INNER JOIN exercise_muscle_roles r ON r.muscleSlug = m.muscleSlug AND r.role = 'TARGET'
        INNER JOIN exercise_kinds e ON e.id = r.exerciseKindId AND e.isArchived = 0
        ORDER BY m.displayName
        """,
    )
    fun observeTargetMuscles(): Flow<List<MuscleFilterOption>>

    @Query("SELECT * FROM exercise_kinds WHERE isArchived = 0 ORDER BY isBuiltin DESC, displayName ASC")
    fun observeAllActive(): Flow<List<ExerciseKindEntity>>

    @Query("SELECT * FROM exercise_kinds WHERE id = :id")
    suspend fun getById(id: Long): ExerciseKindEntity?

    @Query("SELECT * FROM exercise_kinds WHERE catalogId = :catalogId")
    suspend fun getByCatalogId(catalogId: String): ExerciseKindEntity?

    @Query("SELECT COUNT(*) FROM exercise_kinds")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: ExerciseKindEntity): Long

    @Update
    suspend fun update(entity: ExerciseKindEntity)

    @Query("UPDATE exercise_kinds SET isArchived = 1, updatedAtEpochMs = :updatedAt WHERE id = :id")
    suspend fun archive(id: Long, updatedAt: Long)
}

@Dao
interface ExerciseCatalogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertClassification(entity: ExerciseClassificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInstructions(entities: List<ExerciseInstructionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertComments(entities: List<ExerciseCommentEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMuscles(entities: List<MuscleVocabularyEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMuscleRoles(entities: List<ExerciseMuscleRoleEntity>)

    @Query("DELETE FROM exercise_instructions WHERE exerciseKindId = :exerciseKindId")
    suspend fun deleteInstructions(exerciseKindId: Long)

    @Query("DELETE FROM exercise_comments WHERE exerciseKindId = :exerciseKindId")
    suspend fun deleteComments(exerciseKindId: Long)

    @Query("DELETE FROM exercise_muscle_roles WHERE exerciseKindId = :exerciseKindId")
    suspend fun deleteMuscleRoles(exerciseKindId: Long)

    @Query("SELECT * FROM exercise_classifications WHERE exerciseKindId = :exerciseKindId")
    suspend fun getClassification(exerciseKindId: Long): ExerciseClassificationEntity?

    @Query("SELECT * FROM exercise_instructions WHERE exerciseKindId = :exerciseKindId ORDER BY sortOrder")
    suspend fun getInstructions(exerciseKindId: Long): List<ExerciseInstructionEntity>

    @Query("SELECT * FROM exercise_comments WHERE exerciseKindId = :exerciseKindId ORDER BY sortOrder")
    suspend fun getComments(exerciseKindId: Long): List<ExerciseCommentEntity>

    @Query(
        """
        SELECT m.displayName, r.role
        FROM exercise_muscle_roles r
        INNER JOIN muscle_vocabulary m ON m.muscleSlug = r.muscleSlug
        WHERE r.exerciseKindId = :exerciseKindId
        ORDER BY
            CASE r.role
                WHEN 'TARGET' THEN 0
                WHEN 'SYNERGIST' THEN 1
                WHEN 'STABILIZER' THEN 2
                ELSE 3
            END,
            m.displayName
        """,
    )
    suspend fun getMuscleRoles(exerciseKindId: Long): List<MuscleRoleRow>
}

data class MuscleRoleRow(
    val displayName: String,
    val role: String,
)

data class MuscleFilterOption(
    val muscleSlug: String,
    val displayName: String,
)

@Dao
interface AppMetadataDao {
    @Query("SELECT value FROM app_metadata WHERE key = :key")
    suspend fun getValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(entity: AppMetadataEntity)
}
