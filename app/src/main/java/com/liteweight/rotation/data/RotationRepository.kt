package com.liteweight.rotation.data

import com.liteweight.core.data.LiteWeightDatabase
import com.liteweight.program.data.local.ActiveProgramEntity
import com.liteweight.rotation.data.local.RotationPlanEntity
import com.liteweight.rotation.data.local.RotationSlotEntity
import com.liteweight.rotation.domain.CadenceType
import com.liteweight.rotation.domain.RotationPlanDetail
import com.liteweight.rotation.domain.RotationPlanSummary
import com.liteweight.rotation.domain.RotationResolution
import com.liteweight.rotation.domain.RotationResolver
import com.liteweight.rotation.domain.RotationSlotDetail
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RotationRepository
    @Inject
    constructor(
        private val database: LiteWeightDatabase,
    ) {
        private val rotationDao get() = database.rotationDao()
        private val programDao get() = database.programDao()

        fun observePlans(): Flow<List<RotationPlanSummary>> =
            rotationDao.observePlanSummaries().map { rows ->
                rows.map {
                    RotationPlanSummary(
                        id = it.id,
                        name = it.name,
                        cadenceType = it.cadenceType,
                        cadenceInterval = it.cadenceInterval,
                        slotCount = it.slotCount,
                        isActive = it.isActive,
                    )
                }
            }

        suspend fun getPlanDetail(planId: Long): RotationPlanDetail? {
            val plan = rotationDao.getPlan(planId) ?: return null
            val slots =
                rotationDao.getSlots(planId).map { row ->
                    RotationSlotDetail(
                        id = row.id,
                        sortOrder = row.sortOrder,
                        programId = row.programId,
                        programName = row.programName,
                        label = row.label,
                    )
                }
            return RotationPlanDetail(
                id = plan.id,
                name = plan.name,
                cadenceType = plan.cadenceType,
                cadenceInterval = plan.cadenceInterval,
                anchorEpochMs = plan.anchorEpochMs,
                slots = slots,
            )
        }

        suspend fun createPlan(
            name: String,
            cadenceType: CadenceType,
            cadenceInterval: Int,
        ): Long {
            val now = System.currentTimeMillis()
            return rotationDao.insertPlan(
                RotationPlanEntity(
                    name = name.trim(),
                    cadenceType = cadenceType,
                    cadenceInterval = cadenceInterval,
                    anchorEpochMs = now,
                ),
            )
        }

        suspend fun addSlot(
            planId: Long,
            programId: Long,
            label: String?,
        ) {
            val sortOrder = rotationDao.maxSlotSortOrder(planId) + 1
            rotationDao.insertSlot(
                RotationSlotEntity(
                    rotationPlanId = planId,
                    sortOrder = sortOrder,
                    programId = programId,
                    label = label?.trim(),
                ),
            )
        }

        suspend fun activatePlan(planId: Long) {
            val slots = rotationDao.getSlots(planId)
            val firstProgram = slots.firstOrNull()?.programId ?: return
            programDao.setActiveProgram(
                ActiveProgramEntity(
                    programId = firstProgram,
                    rotationPlanId = planId,
                    currentDayIndex = 0,
                    currentLevelIndex = 0,
                    sessionsAtLevel = 0,
                    activatedAtEpochMs = System.currentTimeMillis(),
                ),
            )
        }

        suspend fun resolve(planId: Long): RotationResolution? {
            val plan = rotationDao.getPlan(planId) ?: return null
            val slots = rotationDao.getSlots(planId)
            if (slots.isEmpty()) return null
            val names = slots.associate { it.programId to it.programName }
            val base =
                RotationResolver.resolve(
                    cadenceType = plan.cadenceType,
                    cadenceInterval = plan.cadenceInterval,
                    anchorEpochMs = plan.anchorEpochMs,
                    slotProgramIds = slots.map { it.programId },
                    slotLabels = slots.map { it.label },
                    programNames = names,
                    nowEpochMs = System.currentTimeMillis(),
                ) ?: return null
            return base.copy(planId = plan.id, planName = plan.name)
        }

        suspend fun resolveActive(): RotationResolution? {
            val active = programDao.getActiveProgram() ?: return null
            val planId = active.rotationPlanId ?: return null
            return resolve(planId)
        }
    }
