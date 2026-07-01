package com.liteweight.rotation.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.liteweight.rotation.domain.CadenceType
import kotlinx.coroutines.flow.Flow

data class RotationPlanSummaryRow(
    val id: Long,
    val name: String,
    val cadenceType: CadenceType,
    val cadenceInterval: Int,
    val slotCount: Int,
    val isActive: Boolean,
)

data class RotationSlotRow(
    val id: Long,
    val sortOrder: Int,
    val programId: Long,
    val programName: String,
    val label: String?,
)

@Dao
interface RotationDao {
    @Query(
        """
        SELECT p.id, p.name, p.cadenceType, p.cadenceInterval,
               (SELECT COUNT(*) FROM rotation_slots s WHERE s.rotationPlanId = p.id) AS slotCount,
               CASE WHEN ap.rotationPlanId = p.id THEN 1 ELSE 0 END AS isActive
        FROM rotation_plans p
        LEFT JOIN active_program ap ON ap.id = 1
        ORDER BY isActive DESC, p.name ASC
        """,
    )
    fun observePlanSummaries(): Flow<List<RotationPlanSummaryRow>>

    @Query("SELECT * FROM rotation_plans WHERE id = :id")
    suspend fun getPlan(id: Long): RotationPlanEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPlan(entity: RotationPlanEntity): Long

    @Query(
        """
        SELECT s.id, s.sortOrder, s.programId, pr.name AS programName, s.label
        FROM rotation_slots s
        INNER JOIN programs pr ON pr.id = s.programId
        WHERE s.rotationPlanId = :planId
        ORDER BY s.sortOrder
        """,
    )
    suspend fun getSlots(planId: Long): List<RotationSlotRow>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSlot(entity: RotationSlotEntity): Long

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM rotation_slots WHERE rotationPlanId = :planId")
    suspend fun maxSlotSortOrder(planId: Long): Int
}
