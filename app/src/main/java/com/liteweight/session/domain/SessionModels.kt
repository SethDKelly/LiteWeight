package com.liteweight.session.domain

data class SessionSummary(
    val id: Long,
    val startedAtEpochMs: Long,
    val completedAtEpochMs: Long?,
    val exerciseCount: Int,
)
