package com.wafflestudio.internhasha.applicant.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface ApplicantRepository : JpaRepository<ApplicantEntity, String> {
    fun findByUserId(userId: String): ApplicantEntity?
}
