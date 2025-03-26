package com.waffletoy.team1server.company.persistence

import com.waffletoy.team1server.auth.persistence.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CompanyRepository : JpaRepository<CompanyEntity, String> {
    fun findAllByUser(user: UserEntity): List<CompanyEntity>
}
