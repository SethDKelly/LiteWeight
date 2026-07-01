package com.liteweight.exercise.data.local

import androidx.room.TypeConverter
import com.liteweight.exercise.domain.BodyPosition
import com.liteweight.exercise.domain.Equipment
import com.liteweight.exercise.domain.GripOrientation
import com.liteweight.exercise.domain.GripWidth
import com.liteweight.exercise.domain.NamingMode
import com.liteweight.exercise.domain.PrimaryMovement
import com.liteweight.exercise.domain.UnitType

class ExerciseTypeConverters {
    @TypeConverter fun fromNamingMode(value: NamingMode): String = value.name

    @TypeConverter fun toNamingMode(value: String): NamingMode = NamingMode.valueOf(value)

    @TypeConverter fun fromUnitType(value: UnitType): String = value.name

    @TypeConverter fun toUnitType(value: String): UnitType = UnitType.valueOf(value)

    @TypeConverter fun fromEquipment(value: Equipment?): String? = value?.name

    @TypeConverter fun toEquipment(value: String?): Equipment? = value?.let(Equipment::valueOf)

    @TypeConverter fun fromBodyPosition(value: BodyPosition?): String? = value?.name

    @TypeConverter fun toBodyPosition(value: String?): BodyPosition? = value?.let(BodyPosition::valueOf)

    @TypeConverter fun fromPrimaryMovement(value: PrimaryMovement?): String? = value?.name

    @TypeConverter fun toPrimaryMovement(value: String?): PrimaryMovement? = value?.let(PrimaryMovement::valueOf)

    @TypeConverter fun fromGripWidth(value: GripWidth?): String? = value?.name

    @TypeConverter fun toGripWidth(value: String?): GripWidth? = value?.let(GripWidth::valueOf)

    @TypeConverter fun fromGripOrientation(value: GripOrientation?): String? = value?.name

    @TypeConverter fun toGripOrientation(value: String?): GripOrientation? = value?.let(GripOrientation::valueOf)
}
