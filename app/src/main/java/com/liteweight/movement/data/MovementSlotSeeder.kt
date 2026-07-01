package com.liteweight.movement.data

import com.liteweight.core.data.LiteWeightDatabase
import com.liteweight.movement.data.local.MovementSlotEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovementSlotSeeder
    @Inject
    constructor(
        private val database: LiteWeightDatabase,
    ) {
        private val dao get() = database.movementSlotDao()

        suspend fun seedIfNeeded() {
            if (dao.count() > 0) return
            dao.upsertAll(
                listOf(
                    slot("main_squat", "Main squat", "lw:barbell-squat", "quadriceps"),
                    slot("main_hinge", "Main hinge", "lw:conventional-deadlift", "hamstrings"),
                    slot("horizontal_push", "Horizontal push", "lw:barbell-bench-press", "chest"),
                    slot("vertical_push", "Vertical push", "lw:overhead-press", "shoulders"),
                    slot("horizontal_pull", "Horizontal pull", "lw:barbell-row", "lats"),
                    slot("vertical_pull", "Vertical pull", "lw:lat-pulldown", "lats"),
                    slot("quad_accessory", "Quad accessory", "lw:leg-extension", "quadriceps"),
                    slot("hamstring_accessory", "Hamstring accessory", "lw:leg-curl", "hamstrings"),
                    slot("arm_accessory", "Arm accessory", "lw:barbell-curl", "biceps"),
                ),
            )
        }

        private fun slot(
            roleKey: String,
            displayName: String,
            catalogId: String,
            muscleSlug: String,
        ) = MovementSlotEntity(roleKey, displayName, catalogId, muscleSlug)
    }
