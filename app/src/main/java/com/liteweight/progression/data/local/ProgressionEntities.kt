package com.liteweight.progression.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.liteweight.progression.domain.AdvancementRuleType

@Entity(
    tableName = "progression_schemes",
)
data class ProgressionSchemeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isBuiltin: Boolean,
    val createdAtEpochMs: Long,
)

@Entity(
    tableName = "progression_levels",
    foreignKeys = [
        ForeignKey(
            entity = ProgressionSchemeEntity::class,
            parentColumns = ["id"],
            childColumns = ["schemeId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["schemeId"])],
)
data class ProgressionLevelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val schemeId: Long,
    val levelIndex: Int,
    val name: String,
    val loadIncrement: Double,
    val targetRepsMin: Int?,
    val targetRepsMax: Int?,
    val sessionsRequired: Int?,
    val advancementRule: AdvancementRuleType,
    val isDeload: Boolean,
)
