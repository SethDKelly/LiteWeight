package com.liteweight.core.data

import com.liteweight.exercise.data.local.AppMetadataEntity
import com.liteweight.exercise.domain.WeightUnit
import com.liteweight.program.domain.PrefillMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository
    @Inject
    constructor(
        private val database: LiteWeightDatabase,
    ) {
        private val metadataDao get() = database.appMetadataDao()

        suspend fun getWeightUnit(): WeightUnit {
            val raw = metadataDao.getValue(KEY_WEIGHT_UNIT)
            return raw?.let { runCatching { WeightUnit.valueOf(it) }.getOrNull() } ?: WeightUnit.KG
        }

        suspend fun setWeightUnit(unit: WeightUnit) {
            metadataDao.put(AppMetadataEntity(KEY_WEIGHT_UNIT, unit.name))
        }

        suspend fun getPrefillMode(): PrefillMode {
            val raw = metadataDao.getValue(KEY_PREFILL_MODE)
            return raw?.let { runCatching { PrefillMode.valueOf(it) }.getOrNull() } ?: PrefillMode.CARRY_LAST_SUCCESS
        }

        suspend fun setPrefillMode(mode: PrefillMode) {
            metadataDao.put(AppMetadataEntity(KEY_PREFILL_MODE, mode.name))
        }

        fun weightLabel(unit: WeightUnit): String = if (unit == WeightUnit.KG) "kg" else "lb"

        companion object {
            const val KEY_WEIGHT_UNIT = "weight_unit"
            const val KEY_PREFILL_MODE = "prefill_mode"
        }
    }
