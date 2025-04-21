package com.waffletoy.internhasha.company.persistence

import com.waffletoy.internhasha.auth.persistence.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CompanyRepository : JpaRepository<CompanyEntity, String> {
    fun findAllByUser(user: UserEntity): List<CompanyEntity>
}
