package com.wafflestudio.internhasha.applicant.domain

import com.wafflestudio.internhasha.auth.domain.UserRole
import com.wafflestudio.internhasha.common.domain.Link
import java.time.LocalDateTime

data class Applicant(
    val id: String,
    val name: String,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
    val userRole: UserRole,
    val email: String? = null,
    val enrollYear: Int? = null,
    val department: String? = null,
    val positions: List<String>? = null,
    val slogan: String? = null,
    val explanation: String? = null,
    val stacks: List<String>? = null,
    val profileImageKey: String? = null,
    val cvKey: String? = null,
    val portfolioKey: String? = null,
    val links: List<Link>? = null,
)
