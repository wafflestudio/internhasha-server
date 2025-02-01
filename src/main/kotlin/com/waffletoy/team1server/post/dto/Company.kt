package com.waffletoy.team1server.post.dto

import com.waffletoy.team1server.post.Series
import com.waffletoy.team1server.post.persistence.CompanyEntity
import java.time.LocalDateTime

class Company(
    val id: String,
    val companyName: String,
    val email: String,
    val series: Series,
    val explanation: String? = null,
    val slogan: String? = null,
    // Defaults to 0 if not provided
    val investAmount: Int? = null,
    val investCompany: String? = null,
    val imageLink: FileInfo,
    val irDeckLink: FileInfo,
    val landingPageLink: String? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val links: List<Link> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val positions: List<Position>,
) {
    companion object {
        fun fromEntity(entity: CompanyEntity): Company {
            return Company(
                id = entity.id,
                companyName = entity.companyName,
                email = entity.email,
                series = entity.series,
                explanation = entity.explanation,
                slogan = entity.slogan,
                // Optional field
                investAmount = entity.investAmount.takeIf { it > 0 },
                investCompany = entity.investCompany,
                imageLink =
                    FileInfo(
                        entity.imageFileName ?: "",
                        entity.imageFileType ?: "",
                    ),
                irDeckLink =
                    FileInfo(
                        entity.irDeckFileName ?: "",
                        entity.irDeckFileType ?: "",
                    ),
                landingPageLink = entity.landingPageLink,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
                links = entity.links.map { Link.fromVo(it) },
                tags = entity.tags.map { Tag.fromVo(it) },
                positions = entity.positions.map { Position.fromEntity(it) },
            )
        }
    }
}
