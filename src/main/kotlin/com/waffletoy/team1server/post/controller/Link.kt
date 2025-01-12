package com.waffletoy.team1server.post.controller

import com.waffletoy.team1server.post.persistence.LinkEntity

data class Link(
    val link: String,
    val description: String,
) {
    companion object {
        fun fromEntity(entity: LinkEntity): Link =
            Link(
                link = entity.link,
                description = entity.description ?: entity.link,
            )
    }
}

// 표시할 텍스트가 없으면 링크 자체로 표시
