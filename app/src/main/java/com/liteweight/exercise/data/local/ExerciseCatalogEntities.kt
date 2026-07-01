package com.liteweight.exercise.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise_classifications",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseKindEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseKindId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["exerciseKindId"], unique = true)],
)
data class ExerciseClassificationEntity(
    @PrimaryKey val exerciseKindId: Long,
    val utility: String?,
    val mechanics: String?,
    val force: String?,
    val difficulty: String?,
    val exrxCategoryPath: String?,
)

@Entity(
    tableName = "exercise_instructions",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseKindEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseKindId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["exerciseKindId"])],
)
data class ExerciseInstructionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseKindId: Long,
    val sortOrder: Int,
    val text: String,
)

@Entity(
    tableName = "exercise_comments",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseKindEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseKindId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["exerciseKindId"])],
)
data class ExerciseCommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseKindId: Long,
    val sortOrder: Int,
    val text: String,
)

@Entity(tableName = "muscle_vocabulary")
data class MuscleVocabularyEntity(
    @PrimaryKey val muscleSlug: String,
    val displayName: String,
    val bodyRegion: String?,
)

@Entity(
    tableName = "exercise_muscle_roles",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseKindEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseKindId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = MuscleVocabularyEntity::class,
            parentColumns = ["muscleSlug"],
            childColumns = ["muscleSlug"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["exerciseKindId"]), Index(value = ["muscleSlug"])],
)
data class ExerciseMuscleRoleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseKindId: Long,
    val muscleSlug: String,
    val role: String,
)

@Entity(tableName = "app_metadata")
data class AppMetadataEntity(
    @PrimaryKey val key: String,
    val value: String,
)
