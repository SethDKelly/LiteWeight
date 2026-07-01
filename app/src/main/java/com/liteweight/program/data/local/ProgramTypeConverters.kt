package com.liteweight.program.data.local

import androidx.room.TypeConverter
import com.liteweight.program.domain.ProgramSourceType

class ProgramTypeConverters {
    @TypeConverter
    fun fromProgramSourceType(value: ProgramSourceType): String = value.name

    @TypeConverter
    fun toProgramSourceType(value: String): ProgramSourceType = ProgramSourceType.valueOf(value)
}
