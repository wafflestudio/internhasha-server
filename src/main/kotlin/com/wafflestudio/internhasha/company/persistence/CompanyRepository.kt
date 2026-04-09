package com.wafflestudio.internhasha.company.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CompanyRepository : JpaRepository<CompanyEntity, String> {
    fun findByIdAndDeletedAtIsNull(companyId: String): CompanyEntity?
}
