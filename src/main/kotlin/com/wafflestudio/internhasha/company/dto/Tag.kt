package com.wafflestudio.internhasha.company.dto

data class Tag(
    val tag: String,
) {
    companion object {
        fun fromVo(tagVo: TagVo): Tag {
            return Tag(tag = tagVo.tag)
        }
    }
}
