package com.liteweight.substitution.data

import com.liteweight.core.data.LiteWeightDatabase
import com.liteweight.exercise.domain.ExerciseSummary
import com.liteweight.exercise.domain.toSummary
import com.liteweight.substitution.data.local.SubstitutionGroupEntity
import com.liteweight.substitution.data.local.SubstitutionMemberEntity
import com.liteweight.substitution.domain.SubstitutionGroupDetail
import com.liteweight.substitution.domain.SubstitutionGroupSummary
import com.liteweight.substitution.domain.SubstitutionMember
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class SubstitutionRepository
    @Inject
    constructor(
        private val database: LiteWeightDatabase,
    ) {
        private val substitutionDao get() = database.substitutionDao()

        fun observeGroups(): Flow<List<SubstitutionGroupSummary>> =
            substitutionDao.observeGroups().map { rows ->
                rows.map { SubstitutionGroupSummary(it.id, it.name, it.memberCount) }
            }

        suspend fun getGroupDetail(groupId: Long): SubstitutionGroupDetail? {
            val group = substitutionDao.getGroup(groupId) ?: return null
            val members =
                substitutionDao.getMembers(groupId).map { row ->
                    SubstitutionMember(row.id, row.exerciseKindId, row.displayName)
                }
            return SubstitutionGroupDetail(group.id, group.name, members)
        }

        suspend fun createGroup(name: String): Long =
            substitutionDao.insertGroup(SubstitutionGroupEntity(name = name.trim()))

        suspend fun addMember(
            groupId: Long,
            exerciseKindId: Long,
        ) {
            val sortOrder = substitutionDao.maxMemberSortOrder(groupId) + 1
            substitutionDao.insertMember(
                SubstitutionMemberEntity(
                    groupId = groupId,
                    exerciseKindId = exerciseKindId,
                    sortOrder = sortOrder,
                ),
            )
        }

        suspend fun suggestFromGroups(exerciseKindId: Long): List<ExerciseSummary> =
            substitutionDao.findGroupSubstitutes(exerciseKindId).map { it.toSummary() }
    }
