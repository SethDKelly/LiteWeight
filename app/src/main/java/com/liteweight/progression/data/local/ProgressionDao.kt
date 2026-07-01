package com.liteweight.progression.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.liteweight.progression.domain.AdvancementRuleType
import kotlinx.coroutines.flow.Flow

data class ProgressionSchemeSummaryRow(
    val id: Long,
    val name: String,
    val levelCount: Int,
)

@Dao
interface ProgressionDao {
    @Query(
        """
        SELECT s.id, s.name,
               (SELECT COUNT(*) FROM progression_levels l WHERE l.schemeId = s.id) AS levelCount
        FROM progression_schemes s
        ORDER BY s.isBuiltin DESC, s.name ASC
        """,
    )
    fun observeSchemeSummaries(): Flow<List<ProgressionSchemeSummaryRow>>

    @Query("SELECT * FROM progression_schemes WHERE id = :id")
    suspend fun getScheme(id: Long): ProgressionSchemeEntity?

    @Query("SELECT * FROM progression_levels WHERE schemeId = :schemeId ORDER BY levelIndex")
    suspend fun getLevels(schemeId: Long): List<ProgressionLevelEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertScheme(entity: ProgressionSchemeEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertLevel(entity: ProgressionLevelEntity): Long

    @Query("SELECT COUNT(*) FROM progression_schemes")
    suspend fun schemeCount(): Int
}
