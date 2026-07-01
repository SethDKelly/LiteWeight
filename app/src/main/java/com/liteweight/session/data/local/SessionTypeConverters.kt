package com.liteweight.session.data.local

import androidx.room.TypeConverter

class SessionTypeConverters {
    @TypeConverter
    fun fromSessionStatus(value: SessionStatus): String = value.name

    @TypeConverter
    fun toSessionStatus(value: String): SessionStatus = SessionStatus.valueOf(value)
}
