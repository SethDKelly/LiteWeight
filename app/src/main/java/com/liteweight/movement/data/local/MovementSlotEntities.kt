package com.liteweight.movement.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movement_slots")
data class MovementSlotEntity(
    @PrimaryKey val roleKey: String,
    val displayName: String,
    val defaultCatalogId: String?,
    val muscleSlug: String?,
)
