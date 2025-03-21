package com.waffletoy.team1server.applicant.persistence

import com.waffletoy.team1server.auth.dto.User
import org.springframework.data.jpa.repository.JpaRepository

interface ApplicantRepository : JpaRepository<ApplicantEntity, String> {
    fun findByUser(user: User): ApplicantEntity
}
