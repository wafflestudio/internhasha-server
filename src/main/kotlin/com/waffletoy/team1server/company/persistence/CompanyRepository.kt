package com.waffletoy.team1server.company.persistence

import com.waffletoy.team1server.auth.persistence.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CompanyRepository : JpaRepository<CompanyEntity, String> {
    /**
     * Checks if a company with the given email exists.
     *
     * @param email The email to check.
     * @return True if a company with the email exists, else false.
     */
    fun existsByEmail(email: String): Boolean

    fun findAllByUser(user: UserEntity): List<CompanyEntity>
}
