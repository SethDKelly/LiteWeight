package com.liteweight.analytics.data.local

import androidx.room.TypeConverter
import com.liteweight.analytics.domain.PrType

class AnalyticsTypeConverters {
    @TypeConverter
    fun fromPrType(value: PrType): String = value.name

    @TypeConverter
    fun toPrType(value: String): PrType = PrType.valueOf(value)
}
