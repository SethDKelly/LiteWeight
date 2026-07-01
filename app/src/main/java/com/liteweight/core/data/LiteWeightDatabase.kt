package com.liteweight.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.liteweight.analytics.data.local.AnalyticsDao
import com.liteweight.analytics.data.local.AnalyticsTypeConverters
import com.liteweight.analytics.data.local.ExerciseAnalyticsSummaryEntity
import com.liteweight.analytics.data.local.ExercisePrEventEntity
import com.liteweight.analytics.data.local.ExerciseSessionSnapshotEntity
import com.liteweight.exercise.data.local.AppMetadataDao
import com.liteweight.exercise.data.local.ExerciseCatalogDao
import com.liteweight.exercise.data.local.ExerciseClassificationEntity
import com.liteweight.exercise.data.local.ExerciseCommentEntity
import com.liteweight.exercise.data.local.ExerciseInstructionEntity
import com.liteweight.exercise.data.local.ExerciseKindDao
import com.liteweight.exercise.data.local.ExerciseKindEntity
import com.liteweight.exercise.data.local.ExerciseMuscleRoleEntity
import com.liteweight.exercise.data.local.ExerciseTypeConverters
import com.liteweight.exercise.data.local.AppMetadataEntity
import com.liteweight.exercise.data.local.MuscleVocabularyEntity
import com.liteweight.program.data.local.ActiveProgramEntity
import com.liteweight.program.data.local.InstalledPresetEntity
import com.liteweight.program.data.local.ProgramDao
import com.liteweight.program.data.local.ProgramDayEntity
import com.liteweight.program.data.local.ProgramEntity
import com.liteweight.program.data.local.ProgramExerciseEntity
import com.liteweight.program.data.local.ProgramTypeConverters
import com.liteweight.progression.data.local.ProgressionDao
import com.liteweight.progression.data.local.ProgressionLevelEntity
import com.liteweight.progression.data.local.ProgressionSchemeEntity
import com.liteweight.progression.data.local.ProgressionTypeConverters
import com.liteweight.movement.data.local.MovementSlotDao
import com.liteweight.movement.data.local.MovementSlotEntity
import com.liteweight.rotation.data.local.RotationDao
import com.liteweight.rotation.data.local.RotationPlanEntity
import com.liteweight.rotation.data.local.RotationSlotEntity
import com.liteweight.rotation.data.local.RotationTypeConverters
import com.liteweight.substitution.data.local.SubstitutionDao
import com.liteweight.substitution.data.local.SubstitutionGroupEntity
import com.liteweight.substitution.data.local.SubstitutionMemberEntity
import com.liteweight.session.data.local.SessionTypeConverters
import com.liteweight.session.data.local.WorkoutExerciseEntryEntity
import com.liteweight.session.data.local.WorkoutSessionDao
import com.liteweight.session.data.local.WorkoutSessionEntity
import com.liteweight.session.data.local.WorkoutSetEntryEntity

@Database(
    entities = [
        ExerciseKindEntity::class,
        ExerciseClassificationEntity::class,
        ExerciseInstructionEntity::class,
        ExerciseCommentEntity::class,
        MuscleVocabularyEntity::class,
        ExerciseMuscleRoleEntity::class,
        AppMetadataEntity::class,
        WorkoutSessionEntity::class,
        WorkoutExerciseEntryEntity::class,
        WorkoutSetEntryEntity::class,
        ProgramEntity::class,
        ProgramDayEntity::class,
        ProgramExerciseEntity::class,
        ActiveProgramEntity::class,
        InstalledPresetEntity::class,
        ProgressionSchemeEntity::class,
        ProgressionLevelEntity::class,
        ExerciseSessionSnapshotEntity::class,
        ExercisePrEventEntity::class,
        ExerciseAnalyticsSummaryEntity::class,
        MovementSlotEntity::class,
        SubstitutionGroupEntity::class,
        SubstitutionMemberEntity::class,
        RotationPlanEntity::class,
        RotationSlotEntity::class,
    ],
    version = 6,
    exportSchema = false,
)
@TypeConverters(
    ExerciseTypeConverters::class,
    SessionTypeConverters::class,
    ProgramTypeConverters::class,
    ProgressionTypeConverters::class,
    AnalyticsTypeConverters::class,
    RotationTypeConverters::class,
)
abstract class LiteWeightDatabase : RoomDatabase() {
    abstract fun exerciseKindDao(): ExerciseKindDao

    abstract fun exerciseCatalogDao(): ExerciseCatalogDao

    abstract fun appMetadataDao(): AppMetadataDao

    abstract fun workoutSessionDao(): WorkoutSessionDao

    abstract fun programDao(): ProgramDao

    abstract fun progressionDao(): ProgressionDao

    abstract fun analyticsDao(): AnalyticsDao

    abstract fun movementSlotDao(): MovementSlotDao

    abstract fun substitutionDao(): SubstitutionDao

    abstract fun rotationDao(): RotationDao
}
