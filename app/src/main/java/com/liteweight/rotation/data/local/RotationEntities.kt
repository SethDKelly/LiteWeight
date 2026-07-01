package com.liteweight.rotation.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.liteweight.program.data.local.ProgramEntity
import com.liteweight.rotation.domain.CadenceType

@Entity(tableName = "rotation_plans")
data class RotationPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val cadenceType: CadenceType,
    val cadenceInterval: Int,
    val anchorEpochMs: Long,
)

@Entity(
    tableName = "rotation_slots",
    foreignKeys = [
        ForeignKey(
            entity = RotationPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["rotationPlanId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ProgramEntity::class,
            parentColumns = ["id"],
            childColumns = ["programId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["rotationPlanId"])],
)
data class RotationSlotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rotationPlanId: Long,
    val sortOrder: Int,
    val programId: Long,
    val label: String?,
)
