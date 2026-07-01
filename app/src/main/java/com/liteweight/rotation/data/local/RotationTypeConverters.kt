package com.liteweight.rotation.data.local

import androidx.room.TypeConverter
import com.liteweight.rotation.domain.CadenceType

class RotationTypeConverters {
    @TypeConverter
    fun fromCadenceType(value: CadenceType): String = value.name

    @TypeConverter
    fun toCadenceType(value: String): CadenceType = CadenceType.valueOf(value)
}
