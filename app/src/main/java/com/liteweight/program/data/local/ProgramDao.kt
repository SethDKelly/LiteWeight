package com.liteweight.program.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.liteweight.program.domain.ProgramSourceType
import kotlinx.coroutines.flow.Flow

data class ProgramSummaryRow(
    val id: Long,
    val name: String,
    val sourceType: ProgramSourceType,
    val splitType: String?,
    val dayCount: Int,
    val isActive: Boolean,
)

data class ProgramExerciseRow(
    val id: Long,
    val programDayId: Long,
    val exerciseKindId: Long,
    val displayName: String,
    val movementSlotId: String?,
    val movementSlotLabel: String?,
    val sortOrder: Int,
    val targetSets: Int,
    val targetRepsMin: Int?,
    val targetRepsMax: Int?,
    val notes: String?,
)

@Dao
interface ProgramDao {
    @Query(
        """
        SELECT p.id, p.name, p.sourceType, p.splitType,
               (SELECT COUNT(*) FROM program_days d WHERE d.programId = p.id) AS dayCount,
               CASE WHEN a.programId = p.id THEN 1 ELSE 0 END AS isActive
        FROM programs p
        LEFT JOIN active_program a ON a.id = 1
        WHERE p.isArchived = 0
        ORDER BY isActive DESC, p.name ASC
        """,
    )
    fun observeProgramSummaries(): Flow<List<ProgramSummaryRow>>

    @Query("SELECT * FROM programs WHERE id = :id")
    suspend fun getProgram(id: Long): ProgramEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProgram(entity: ProgramEntity): Long

    @Update
    suspend fun updateProgram(entity: ProgramEntity)

    @Query("SELECT * FROM program_days WHERE programId = :programId ORDER BY dayIndex")
    suspend fun getDays(programId: Long): List<ProgramDayEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDay(entity: ProgramDayEntity): Long

    @Query(
        """
        SELECT pe.id, pe.programDayId, pe.exerciseKindId, ek.displayName,
               pe.movementSlotId, ms.displayName AS movementSlotLabel, pe.sortOrder,
               pe.targetSets, pe.targetRepsMin, pe.targetRepsMax, pe.notes
        FROM program_exercises pe
        INNER JOIN exercise_kinds ek ON ek.id = pe.exerciseKindId
        LEFT JOIN movement_slots ms ON ms.roleKey = pe.movementSlotId
        WHERE pe.programDayId = :programDayId
        ORDER BY pe.sortOrder
        """,
    )
    suspend fun getExercisesForDay(programDayId: Long): List<ProgramExerciseRow>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExercise(entity: ProgramExerciseEntity): Long

    @Query("SELECT * FROM active_program WHERE id = 1")
    suspend fun getActiveProgram(): ActiveProgramEntity?

    @Query("SELECT * FROM active_program WHERE id = 1")
    fun observeActiveProgram(): Flow<ActiveProgramEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setActiveProgram(entity: ActiveProgramEntity)

    @Query("UPDATE active_program SET currentDayIndex = :dayIndex WHERE id = 1")
    suspend fun updateActiveDayIndex(dayIndex: Int)

    @Query("UPDATE active_program SET currentLevelIndex = :levelIndex, sessionsAtLevel = :sessionsAtLevel WHERE id = 1")
    suspend fun updateActiveLevel(levelIndex: Int, sessionsAtLevel: Int)

    @Query("UPDATE programs SET progressionSchemeId = :schemeId, updatedAtEpochMs = :updatedAt WHERE id = :programId")
    suspend fun assignProgressionScheme(programId: Long, schemeId: Long?, updatedAt: Long)

    @Query("SELECT programId FROM installed_presets WHERE presetId = :presetId")
    suspend fun getInstalledProgramId(presetId: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markPresetInstalled(entity: InstalledPresetEntity)

    @Query("SELECT COALESCE(MAX(dayIndex), -1) FROM program_days WHERE programId = :programId")
    suspend fun maxDayIndex(programId: Long): Int

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM program_exercises WHERE programDayId = :programDayId")
    suspend fun maxExerciseSortOrder(programDayId: Long): Int

    @Query("UPDATE program_exercises SET exerciseKindId = :exerciseKindId WHERE id = :programExerciseId")
    suspend fun updateProgramExerciseKind(programExerciseId: Long, exerciseKindId: Long)
}
