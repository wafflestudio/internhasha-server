package com.wafflestudio.internhasha.company.service.dto

import com.wafflestudio.internhasha.common.dto.Link
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
)
