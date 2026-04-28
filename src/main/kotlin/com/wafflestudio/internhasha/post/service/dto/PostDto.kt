package com.wafflestudio.internhasha.post.service.dto

import com.wafflestudio.internhasha.company.service.dto.CompanyDto
import com.wafflestudio.internhasha.post.domain.Position

data class PostDto(
    // 직군 정보(회사 포함)
    val position: PositionDto,
    // 북마크 여부
    val isBookmarked: Boolean = false,
    // 해당 공고에 신청된 커피챗 개수
    val coffeeChatCount: Long,
) {
    companion object {
        fun fromEntity(
            entity: Position,
            isBookmarked: Boolean,
            coffeeChatCount: Long,
            isLoggedIn: Boolean = false,
        ): PostDto {
            return when (isLoggedIn) {
                true ->
                    PostDto(
                        position = PositionDto.fromEntity(entity),
                        isBookmarked = isBookmarked,
                        coffeeChatCount = coffeeChatCount,
                    )
                false -> {
                    val newCompany =
                        CompanyDto.fromEntity(entity.company).copy(
                            companyInfoPDFKey = null,
                        )
                    val newPosition =
                        PositionDto.fromEntity(entity).copy(
                            company = newCompany,
                        )
                    PostDto(
                        position = newPosition,
                        isBookmarked = isBookmarked,
                        coffeeChatCount = coffeeChatCount,
                    )
                }
            }
        }
    }
}
