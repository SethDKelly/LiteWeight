package com.liteweight.substitution.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.liteweight.exercise.data.local.ExerciseKindEntity

@Entity(tableName = "substitution_groups")
data class SubstitutionGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
)

@Entity(
    tableName = "substitution_members",
    foreignKeys = [
        ForeignKey(
            entity = SubstitutionGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseKindEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseKindId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["groupId"]), Index(value = ["exerciseKindId"])],
)
data class SubstitutionMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val exerciseKindId: Long,
    val sortOrder: Int,
)
