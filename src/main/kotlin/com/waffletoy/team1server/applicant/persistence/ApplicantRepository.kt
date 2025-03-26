package com.waffletoy.team1server.applicant.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface ApplicantRepository : JpaRepository<ApplicantEntity, String> {
    fun findByUserId(userId: String): ApplicantEntity
}
