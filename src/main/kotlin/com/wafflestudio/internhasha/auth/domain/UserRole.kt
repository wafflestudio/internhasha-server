package com.wafflestudio.internhasha.auth.domain

enum class UserRole(
    val value: String,
) {
    ROLE_APPLICANT(RoleValue.ROLE_APPLICANT),
    ROLE_COMPANY(RoleValue.ROLE_COMPANY),
}

object RoleValue {
    const val ROLE_APPLICANT = "ROLE_APPLICANT"
    const val ROLE_COMPANY = "ROLE_COMPANY"
}
