package com.wafflestudio.internhasha.post.controller.dto.response

data class PostBriefResponseWithPage(
    val posts: List<PostBriefResponse>,
    val paginator: Paginator,
)
