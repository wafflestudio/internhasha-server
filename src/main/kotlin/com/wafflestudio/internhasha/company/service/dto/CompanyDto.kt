package com.wafflestudio.internhasha.company.service.dto

import com.wafflestudio.internhasha.common.dto.Link
import com.wafflestudio.internhasha.company.domain.Company
import com.wafflestudio.internhasha.company.domain.Domain
import com.wafflestudio.internhasha.company.domain.Tag

data class CompanyDto(
    val id: String,
    val companyName: String,
    val companyEstablishedYear: Int?,
    val domain: Domain?,
    val headcount: Int?,
    val location: String?,
    val slogan: String?,
    val detail: String?,
    val profileImageKey: String?,
    val companyInfoPDFKey: String?,
    val landingPageLink: String?,
    val links: List<Link>,
    val tags: List<Tag>,
    val vcName: String?,
    val vcRec: String?,
) {
    companion object {
        fun fromEntity(company: Company) =
            CompanyDto(
                id = company.requireId(),
                companyName = company.name,
                companyEstablishedYear = company.companyEstablishedYear,
                domain = company.domain,
                headcount = company.headcount,
                location = company.location,
                slogan = company.slogan,
                detail = company.detail,
                profileImageKey = company.profileImageKey,
                companyInfoPDFKey = company.companyInfoPDFKey,
                landingPageLink = company.landingPageLink,
                links = company.links.map { Link(link = it.link, description = it.description) },
                tags = company.tags.map { Tag(tag = it.tag) },
                vcName = company.vcName,
                vcRec = company.vcRec,
            )
    }
}
