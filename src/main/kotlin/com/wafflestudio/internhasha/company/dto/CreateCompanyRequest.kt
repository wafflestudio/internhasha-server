package com.wafflestudio.internhasha.company.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.wafflestudio.internhasha.company.Domain
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
    val domain: Domain,
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
    val links: List<Link>? = emptyList(),
    @field:Valid
    val tags: List<Tag>? = emptyList(),
)

typealias UpdateCompanyRequest = CreateCompanyRequest
