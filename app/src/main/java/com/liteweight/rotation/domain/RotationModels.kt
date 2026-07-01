package com.liteweight.rotation.domain

enum class CadenceType {
    DAYS,
    WEEKS,
    MONTHS,
}

data class RotationPlanSummary(
    val id: Long,
    val name: String,
    val cadenceType: CadenceType,
    val cadenceInterval: Int,
    val slotCount: Int,
    val isActive: Boolean,
)

data class RotationSlotDetail(
    val id: Long,
    val sortOrder: Int,
    val programId: Long,
    val programName: String,
    val label: String?,
)

data class RotationPlanDetail(
    val id: Long,
    val name: String,
    val cadenceType: CadenceType,
    val cadenceInterval: Int,
    val anchorEpochMs: Long,
    val slots: List<RotationSlotDetail>,
)

data class RotationResolution(
    val planId: Long,
    val planName: String,
    val currentSlotIndex: Int,
    val currentProgramId: Long,
    val currentProgramName: String,
    val currentLabel: String?,
    val nextProgramId: Long,
    val nextProgramName: String,
    val nextLabel: String?,
    val periodEndsAtEpochMs: Long,
)
