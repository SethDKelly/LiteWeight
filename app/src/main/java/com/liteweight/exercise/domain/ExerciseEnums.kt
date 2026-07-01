package com.liteweight.exercise.domain

enum class Equipment {
    BARBELL,
    DUMBBELL,
    CABLE,
    SMITH_MACHINE,
    MACHINE,
    KETTLEBELL,
    BODYWEIGHT,
    BAND,
    OTHER,
}

enum class BodyPosition {
    FLAT_BENCH,
    INCLINE,
    DECLINE,
    SEATED,
    STANDING,
    LYING,
    KNEELING,
    UNILATERAL,
    NONE,
}

enum class PrimaryMovement {
    BENCH_PRESS,
    OVERHEAD_PRESS,
    ROW,
    PULLDOWN,
    SQUAT,
    DEADLIFT,
    LUNGE,
    CURL,
    EXTENSION,
    RAISE,
    FLY,
    CARRY,
    OTHER,
}

enum class GripWidth {
    CLOSE,
    STANDARD,
    WIDE,
    NONE,
}

enum class GripOrientation {
    PRONATED,
    SUPINATED,
    NEUTRAL,
    NONE,
}

enum class NamingMode {
    STRUCTURED,
    FREEFORM,
}

enum class UnitType {
    WEIGHT,
    TIME,
    BODYWEIGHT,
    DISTANCE,
}
