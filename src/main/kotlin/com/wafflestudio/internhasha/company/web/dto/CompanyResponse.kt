package com.wafflestudio.internhasha.company.web.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.internhasha.common.domain.Link
import com.wafflestudio.internhasha.company.domain.Company
import com.wafflestudio.internhasha.company.domain.Domain
import com.wafflestudio.internhasha.company.domain.Tag

@JsonInclude(JsonInclude.Include.NON_NULL)
class CompanyResponse(
    val id: String,
    // 필수 정보
    val companyName: String,
    val companyEstablishedYear: Int?,
    val domain: Domain?,
    val headcount: Int?,
    val location: String?,
    val slogan: String?,
    val detail: String?,
    val profileImageKey: String?,
    // 선택 정보
    val companyInfoPDFKey: String?,
    val landingPageLink: String?,
    val links: List<Link>,
    val tags: List<Tag>,
    val vcName: String?,
    @JsonProperty("vcRecommendation")
    val vcRec: String?,
) {
    companion object {
        fun fromCompany(company: Company) =
            CompanyResponse(
                id = company.id,
                companyName = company.companyName,
                companyEstablishedYear = company.companyEstablishedYear,
                domain = company.domain,
                headcount = company.headcount,
                location = company.location,
                slogan = company.slogan,
                detail = company.detail,
                profileImageKey = company.profileImageKey,
                companyInfoPDFKey = company.companyInfoPDFKey,
                landingPageLink = company.landingPageLink,
                links = company.links,
                tags = company.tags,
                vcName = company.vcName,
                vcRec = company.vcRec,
            )
    }
}
