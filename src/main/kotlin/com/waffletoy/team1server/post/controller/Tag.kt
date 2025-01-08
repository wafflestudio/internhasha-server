package com.waffletoy.team1server.post.controller

import com.waffletoy.team1server.post.persistence.TagEntity

data class Tag(
    val id: String,
    val tag: String,
) {
    companion object {
        fun fromEntity(entity: TagEntity): Tag =
            Tag(
                id = entity.id,
                tag = entity.tag,
            )
    }
}
