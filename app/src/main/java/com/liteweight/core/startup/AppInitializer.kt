package com.liteweight.core.startup

import com.liteweight.exercise.data.catalog.CatalogSeeder
import com.liteweight.movement.data.MovementSlotSeeder
import com.liteweight.progression.data.ProgressionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppInitializer
    @Inject
    constructor(
        private val catalogSeeder: CatalogSeeder,
        private val progressionRepository: ProgressionRepository,
        private val movementSlotSeeder: MovementSlotSeeder,
    ) {
        suspend fun initialize() {
            catalogSeeder.seedIfNeeded()
            movementSlotSeeder.seedIfNeeded()
            progressionRepository.createBuiltinSchemeIfEmpty()
        }
    }
