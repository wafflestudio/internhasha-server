package com.wafflestudio.internhasha.post.domain

data class Post(
    // 직군 정보(회사 포함)
    val position: Position,
    // 북마크 여부
    val isBookmarked: Boolean = false,
    // 해당 공고에 신청된 커피챗 개수
    val coffeeChatCount: Long,
) {
    companion object {
        fun fromPosition(
            position: Position,
            isBookmarked: Boolean,
            coffeeChatCount: Long,
            isLoggedIn: Boolean = false,
        ): Post {
            return when (isLoggedIn) {
                true ->
                    Post(
                        position = position,
                        isBookmarked = isBookmarked,
                        coffeeChatCount = coffeeChatCount,
                    )
                false -> {
                    val newCompany =
                        position.company.copy(
                            companyInfoPDFKey = null,
                        )
                    val newPosition =
                        position.copy(
                            company = newCompany,
                        )
                    Post(
                        position = newPosition,
                        isBookmarked = isBookmarked,
                        coffeeChatCount = coffeeChatCount,
                    )
                }
            }
        }
    }
}
