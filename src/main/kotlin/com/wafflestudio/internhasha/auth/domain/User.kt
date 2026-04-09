package com.wafflestudio.internhasha.auth.domain

sealed class User {
    abstract val id: String
    abstract val name: String
    abstract val userRole: UserRole
    abstract val email: String
    abstract val passwordHash: String?
}

data class UserApplicant(
    override val id: String,
    override val name: String,
    override val userRole: UserRole,
    override val email: String,
    override val passwordHash: String?,
) : User()

data class UserCompany(
    override val id: String,
    override val name: String,
    override val userRole: UserRole,
    override val email: String,
    override val passwordHash: String?,
    val vcName: String?,
    val vcRec: String?,
) : User()
