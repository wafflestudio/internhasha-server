package com.wafflestudio.internhasha.company.dto

data class Link(
    val description: String,
    val link: String,
) {
    companion object {
        fun fromVo(linkVo: LinkVo): Link = Link(linkVo.description, linkVo.link)
    }
}
