package com.wafflestudio.internhasha.company.controller.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.internhasha.common.dto.Link
import com.wafflestudio.internhasha.company.domain.Tag
import com.wafflestudio.internhasha.company.service.dto.CompanyDto

@JsonInclude(JsonInclude.Include.NON_NULL)
class CompanyResponse(
    val id: String,
    // 필수 정보
    val companyName: String,
    val companyEstablishedYear: Int?,
    val domain: String?,
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
        fun fromDto(companyDto: CompanyDto) =
            CompanyResponse(
                id = companyDto.id,
                companyName = companyDto.companyName,
                companyEstablishedYear = companyDto.companyEstablishedYear,
                domain = companyDto.domain?.displayName(),
                headcount = companyDto.headcount,
                location = companyDto.location,
                slogan = companyDto.slogan,
                detail = companyDto.detail,
                profileImageKey = companyDto.profileImageKey,
                companyInfoPDFKey = companyDto.companyInfoPDFKey,
                landingPageLink = companyDto.landingPageLink,
                links = companyDto.links,
                tags = companyDto.tags,
                vcName = companyDto.vcName,
                vcRec = companyDto.vcRec,
            )
    }
}
