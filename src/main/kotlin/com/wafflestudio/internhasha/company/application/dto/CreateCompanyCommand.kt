package com.wafflestudio.internhasha.company.application.dto

import com.wafflestudio.internhasha.common.domain.Link
import com.wafflestudio.internhasha.company.domain.Company
import com.wafflestudio.internhasha.company.domain.Domain
import com.wafflestudio.internhasha.company.domain.Tag

data class CreateCompanyCommand(
    val companyId: String,
    val companyEstablishedYear: Int,
    val domain: Domain,
    val headcount: Int,
    val location: String,
    val slogan: String,
    val detail: String,
    val profileImageKey: String,
    val companyInfoPDFKey: String? = null,
    val landingPageLink: String? = null,
    val links: List<Link>? = emptyList(),
    val tags: List<Tag>? = emptyList(),
) {
    fun toUpdatedDomain(company: Company): Company {
        return company.copy(
            companyEstablishedYear = this.companyEstablishedYear,
            domain = this.domain,
            headcount = this.headcount,
            location = this.location,
            slogan = this.slogan,
            detail = this.detail,
            profileImageKey = this.profileImageKey,
            companyInfoPDFKey = this.companyInfoPDFKey ?: company.companyInfoPDFKey,
            landingPageLink = this.landingPageLink ?: company.landingPageLink,
            links = this.links ?: company.links,
            tags = this.tags ?: company.tags,
        )
    }
}

typealias UpdateCompanyCommand = CreateCompanyCommand
