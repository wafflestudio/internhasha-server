package com.waffletoy.team1server.post.dto

import com.waffletoy.team1server.post.Series
import com.waffletoy.team1server.post.persistence.CompanyEntity
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.URL
import java.time.LocalDateTime

class Company (
    val id: String,
    val companyName: String,
    val email: String,
    val series: Series,
    // Alternatively, consider using an enum type for better type safety
    val explanation: String? = null,
    val slogan: String? = null,
    val investAmount: Int? = null, // Defaults to 0 if not provided
    val investCompany: String? = null,
    val imageLink: String? = null,
    val irDeckLink: String? = null,
    val landingPageLink: String? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val links: List<Link> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val positions: List<Position>
) {
    companion object {
        fun fromEntity(entity: CompanyEntity): Company {
            return Company(
                id = entity.id,
                companyName = entity.companyName,
                email = entity.email,
                series = entity.series, // Assuming Series is an enum
                explanation = entity.explanation,
                slogan = entity.slogan,
                investAmount = entity.investAmount.takeIf { it > 0 }, // Optional field
                investCompany = entity.investCompany,
                imageLink = entity.imageLink,
                irDeckLink = entity.irDeckLink,
                landingPageLink = entity.landingPageLink,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
                links = entity.links.map { Link.fromVo(it) },
                tags = entity.tags.map { Tag.fromVo(it) },
                positions = entity.positions.map { Position.fromEntity(it) }
            )
        }
    }
}