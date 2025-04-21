package com.wafflestudio.internhasha.applicant.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.URL

data class Link(
    @field:NotBlank
    @field:URL(message = "link가 URL의 형식이 아닙니다.")
    @field:Size(message = "link가 최대 글자수를 초과했습니다.", max = 2047)
    val link: String,
    @field:NotBlank
    @field:Size(message = "link의 description이 최대 글자수를 초과했습니다.", max = 30)
    val description: String,
)
