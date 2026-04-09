package com.wafflestudio.internhasha.company.application.port

import com.wafflestudio.internhasha.company.domain.Company

interface CompanyPersistencePort {
    fun findCompanyByCompanyId(companyId: String): Company?

    fun findCompanyByCompanyIdIncludingDeleted(companyId: String): Company?

    fun updateCompany(company: Company): Company
}
