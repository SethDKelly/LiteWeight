package com.liteweight.core.di

import android.content.Context
import androidx.room.Room
import com.liteweight.core.data.LiteWeightDatabase
import com.liteweight.exercise.data.local.AppMetadataDao
import com.liteweight.exercise.data.local.ExerciseCatalogDao
import com.liteweight.exercise.data.local.ExerciseKindDao
import com.liteweight.analytics.data.local.AnalyticsDao
import com.liteweight.program.data.local.ProgramDao
import com.liteweight.movement.data.local.MovementSlotDao
import com.liteweight.progression.data.local.ProgressionDao
import com.liteweight.rotation.data.local.RotationDao
import com.liteweight.substitution.data.local.SubstitutionDao
import com.liteweight.session.data.local.WorkoutSessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): LiteWeightDatabase =
        Room.databaseBuilder(
            context,
            LiteWeightDatabase::class.java,
            "liteweight.db",
        ).fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideExerciseKindDao(database: LiteWeightDatabase): ExerciseKindDao =
        database.exerciseKindDao()

    @Provides
    fun provideExerciseCatalogDao(database: LiteWeightDatabase): ExerciseCatalogDao =
        database.exerciseCatalogDao()

    @Provides
    fun provideAppMetadataDao(database: LiteWeightDatabase): AppMetadataDao =
        database.appMetadataDao()

    @Provides
    fun provideWorkoutSessionDao(database: LiteWeightDatabase): WorkoutSessionDao =
        database.workoutSessionDao()

    @Provides
    fun provideProgramDao(database: LiteWeightDatabase): ProgramDao =
        database.programDao()

    @Provides
    fun provideProgressionDao(database: LiteWeightDatabase): ProgressionDao =
        database.progressionDao()

    @Provides
    fun provideAnalyticsDao(database: LiteWeightDatabase): AnalyticsDao =
        database.analyticsDao()

    @Provides
    fun provideMovementSlotDao(database: LiteWeightDatabase): MovementSlotDao =
        database.movementSlotDao()

    @Provides
    fun provideSubstitutionDao(database: LiteWeightDatabase): SubstitutionDao =
        database.substitutionDao()

    @Provides
    fun provideRotationDao(database: LiteWeightDatabase): RotationDao =
        database.rotationDao()
}
