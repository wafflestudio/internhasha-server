package com.wafflestudio.internhasha.company.repository

import com.wafflestudio.internhasha.company.domain.Company
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CompanyRepository : JpaRepository<Company, String> {
    fun findByIdAndDeletedAtIsNull(companyId: String): Company?
}
