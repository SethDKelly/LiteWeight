package com.liteweight.movement.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MovementSlotDao {
    @Query("SELECT * FROM movement_slots ORDER BY displayName")
    suspend fun getAll(): List<MovementSlotEntity>

    @Query("SELECT * FROM movement_slots WHERE roleKey = :roleKey")
    suspend fun getByKey(roleKey: String): MovementSlotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<MovementSlotEntity>)

    @Query("SELECT COUNT(*) FROM movement_slots")
    suspend fun count(): Int
}
