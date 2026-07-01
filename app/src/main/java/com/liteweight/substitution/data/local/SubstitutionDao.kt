package com.liteweight.substitution.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class SubstitutionGroupSummaryRow(
    val id: Long,
    val name: String,
    val memberCount: Int,
)

data class SubstitutionMemberRow(
    val id: Long,
    val exerciseKindId: Long,
    val displayName: String,
    val sortOrder: Int,
)

@Dao
interface SubstitutionDao {
    @Query(
        """
        SELECT g.id, g.name,
               (SELECT COUNT(*) FROM substitution_members m WHERE m.groupId = g.id) AS memberCount
        FROM substitution_groups g
        ORDER BY g.name
        """,
    )
    fun observeGroups(): Flow<List<SubstitutionGroupSummaryRow>>

    @Query("SELECT * FROM substitution_groups WHERE id = :id")
    suspend fun getGroup(id: Long): SubstitutionGroupEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertGroup(entity: SubstitutionGroupEntity): Long

    @Query(
        """
        SELECT m.id, m.exerciseKindId, e.displayName, m.sortOrder
        FROM substitution_members m
        INNER JOIN exercise_kinds e ON e.id = m.exerciseKindId
        WHERE m.groupId = :groupId
        ORDER BY m.sortOrder
        """,
    )
    suspend fun getMembers(groupId: Long): List<SubstitutionMemberRow>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMember(entity: SubstitutionMemberEntity): Long

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM substitution_members WHERE groupId = :groupId")
    suspend fun maxMemberSortOrder(groupId: Long): Int

    @Query(
        """
        SELECT DISTINCT e.* FROM substitution_members m
        INNER JOIN substitution_members m2 ON m2.groupId = m.groupId
        INNER JOIN exercise_kinds e ON e.id = m.exerciseKindId
        WHERE m2.exerciseKindId = :exerciseKindId
        AND m.exerciseKindId != :exerciseKindId
        AND e.isArchived = 0
        ORDER BY m.sortOrder
        """,
    )
    suspend fun findGroupSubstitutes(exerciseKindId: Long): List<com.liteweight.exercise.data.local.ExerciseKindEntity>
}
