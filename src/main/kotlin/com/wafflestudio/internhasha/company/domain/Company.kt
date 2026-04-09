package com.wafflestudio.internhasha.company.domain

import com.wafflestudio.internhasha.common.domain.Link

data class Company(
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
    val vcRec: String?,
)
