package com.liteweight.progression.data.local

import androidx.room.TypeConverter
import com.liteweight.progression.domain.AdvancementRuleType

class ProgressionTypeConverters {
    @TypeConverter
    fun fromAdvancementRuleType(value: AdvancementRuleType): String = value.name

    @TypeConverter
    fun toAdvancementRuleType(value: String): AdvancementRuleType = AdvancementRuleType.valueOf(value)
}
