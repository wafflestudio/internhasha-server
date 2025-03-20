package com.waffletoy.team1server.post.dto

import com.waffletoy.team1server.post.Series
import com.waffletoy.team1server.post.persistence.CompanyEntity
import java.time.LocalDateTime

class Company(
    val id: String,
    //필수 정보
    val companyName: String,
    val companyEstablishedYear: Int,
    val domain: String,
    val headcount: Int,
    val location: String,
    val slogan: String, //회사 한 줄 소개
    val detail: String, //회사 상세 소개
    val imageKey: String,
    // 선택 정보
    val companyInfoPDFLink: String?, //회사 원페이저 소개 pdf
    val landingPageLink: String, //홈페이지(?)
    val links: List<Link>,
    val tags: List<Tag>,
    val vcName: String,
    val vcRec: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun fromEntity(entity: CompanyEntity): Company {
            return Company(
                id = entity.id,
                companyName = entity.companyName,
                companyEstablishedYear = entity.companyEstablishedYear,
                domain = entity.domain,
                headcount = entity.headcount,
                location = entity.location,
                slogan = entity.slogan,
                detail = entity.detail,
                imageKey = entity.imageKey,
                // Optional field
                companyInfoPDFLink = entity.companyInfoPDFLink,
                landingPageLink = entity.landingPageLink,
                vcName = entity.vcName,
                vcRec = entity.vcRec,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
                links = entity.links.map { Link.fromVo(it) },
                tags = entity.tags.map { Tag.fromVo(it) },
//                positions = entity.positions.map { Position.fromEntity(it) },
            )
        }
    }
}
