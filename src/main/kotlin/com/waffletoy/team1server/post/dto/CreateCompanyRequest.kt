package com.waffletoy.team1server.post.dto

import com.waffletoy.team1server.post.Series
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import org.hibernate.validator.constraints.URL

data class CreateCompanyRequest(
    @field:NotBlank(message = "Company name is required.")
    val companyName: String,
    @field:NotBlank(message = "Email is required.")
    @field:Email(message = "Invalid email format.")
    val email: String,
    @field:NotNull(message = "Series is required.")
    val series: Series,
    // Alternatively, consider using an enum type for better type safety
    val explanation: String? = null,
    val slogan: String? = null,
    // Defaults to 0 if not provided
    @field:Min(value = 0, message = "Invest amount must be non-negative.")
    @field:Max(value = 100000, message = "Invest amount must not exceed 100000")
    val investAmount: Int? = null,
    val investCompany: String? = null,
    // @field:URL(message = "Image link must be a valid URL.")
    val imageLink: String? = null,
    // @field:URL(message = "IR deck link must be a valid URL.")
    val irDeckLink: String? = null,
    @field:URL(message = "Landing page link must be a valid URL.")
    val landingPageLink: String? = null,
    @field:Valid
    val links: List<Link> = emptyList(),
    @field:Valid
    val tags: List<Tag> = emptyList(),
)

typealias UpdateCompanyRequest = CreateCompanyRequest
