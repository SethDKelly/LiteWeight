package com.liteweight.substitution.domain

data class SubstitutionGroupSummary(
    val id: Long,
    val name: String,
    val memberCount: Int,
)

data class SubstitutionGroupDetail(
    val id: Long,
    val name: String,
    val members: List<SubstitutionMember>,
)

data class SubstitutionMember(
    val id: Long,
    val exerciseKindId: Long,
    val displayName: String,
)
