package com.wafflestudio.internhasha.company.persistence

import com.wafflestudio.internhasha.company.application.port.CompanyPersistencePort
import com.wafflestudio.internhasha.company.domain.Company
import com.wafflestudio.internhasha.company.domain.CompanyNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class CompanyPersistenceAdapter(
    private val companyRepository: CompanyRepository,
    private val mapper: CompanyPersistenceMapper,
) : CompanyPersistencePort {
    override fun findCompanyByCompanyId(companyId: String): Company? {
        return companyRepository.findByIdAndDeletedAtIsNull(companyId)?.let { mapper.toCompany(it) }
    }

    override fun findCompanyByCompanyIdIncludingDeleted(companyId: String): Company? {
        return companyRepository.findByIdOrNull(companyId)?.let { mapper.toCompany(it) }
    }

    override fun updateCompany(company: Company): Company {
        val companyEntity =
            companyRepository.findByIdAndDeletedAtIsNull(company.id)
                ?: throw CompanyNotFoundException()
        return companyEntity.apply {
            name?.let { this.name = name }
            companyEstablishedYear?.let { this.companyEstablishedYear = companyEstablishedYear }
            domain?.let { this.domain = domain }
            headcount?.let { this.headcount = headcount }
            location?.let { this.location = location }
            slogan?.let { this.slogan = slogan }
            detail?.let { this.detail = detail }
            profileImageKey?.let { this.profileImageKey = profileImageKey }
            companyInfoPDFKey?.let { this.companyInfoPDFKey = companyInfoPDFKey }
            landingPageLink?.let { this.landingPageLink = landingPageLink }
            vcName?.let { this.vcName = vcName }
            vcRec?.let { this.vcRec = vcRec }
            if (links != null) {
                this.links.clear()
                this.links.addAll(links)
            }
            if (tags != null) {
                this.tags.clear()
                this.tags.addAll(tags)
            }
        }.let { mapper.toCompany(it) }
    }
}
