package com.liteweight.program.domain

import com.liteweight.program.data.local.ActiveProgramEntity
import com.liteweight.rotation.data.RotationRepository

suspend fun effectiveProgramId(
    active: ActiveProgramEntity,
    rotationRepository: RotationRepository,
): Long {
    val planId = active.rotationPlanId ?: return active.programId
    return rotationRepository.resolve(planId)?.currentProgramId ?: active.programId
}
