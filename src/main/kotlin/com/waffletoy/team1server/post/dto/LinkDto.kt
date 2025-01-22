package com.waffletoy.team1server.post.dto

data class LinkDto(
    val description: String,
    val link: String,
) {
    companion object {
        fun fromLink(link: Link): LinkDto = LinkDto(link.description, link.link)
    }
}
