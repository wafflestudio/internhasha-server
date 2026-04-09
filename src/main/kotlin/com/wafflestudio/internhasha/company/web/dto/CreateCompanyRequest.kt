package com.wafflestudio.internhasha.company.web.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.wafflestudio.internhasha.common.domain.Link
import com.wafflestudio.internhasha.common.web.dto.LinkRequest
import com.wafflestudio.internhasha.company.application.dto.CreateCompanyCommand
import com.wafflestudio.internhasha.company.domain.Domain
import com.wafflestudio.internhasha.company.domain.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.URL

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CreateCompanyRequest(
    @field:Min(value = 1800, message = "Established year must be after 1800.")
    val companyEstablishedYear: Int,
    @field:NotNull(message = "Domain is required.")
    var domain: Domain,
    @field:Min(value = 1, message = "Headcount must be at least 1.")
    val headcount: Int,
    @field:NotBlank(message = "Location is required.")
    val location: String,
    @field:NotBlank(message = "Slogan is required.")
    val slogan: String,
    @field:NotBlank(message = "Detail description is required.")
    val detail: String,
    @field:NotBlank(message = "Profile image key is required.")
    val profileImageKey: String,
    val companyInfoPDFKey: String? = null,
    @field:URL(message = "Landing page link must be a valid URL.")
    val landingPageLink: String? = null,
    @field:Valid
    val links: List<LinkRequest>? = emptyList(),
    @field:Valid
    val tags: List<Tag>? = emptyList(),
) {
    fun toCommand(companyId: String) =
        CreateCompanyCommand(
            companyId = companyId,
            companyEstablishedYear = this.companyEstablishedYear,
            domain = this.domain,
            headcount = this.headcount,
            location = this.location,
            slogan = this.slogan,
            detail = this.detail,
            profileImageKey = this.profileImageKey,
            companyInfoPDFKey = this.companyInfoPDFKey,
            landingPageLink = this.landingPageLink,
            links = this.links?.map { Link(link = it.link, description = it.description) },
            tags = this.tags,
        )
}

typealias UpdateCompanyRequest = CreateCompanyRequest
