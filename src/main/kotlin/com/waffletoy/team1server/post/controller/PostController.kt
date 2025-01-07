package com.waffletoy.team1server.post.controller

import com.waffletoy.team1server.post.Category
import com.waffletoy.team1server.post.service.PostService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/post")
class PostController(
    private val postService: PostService,
) {

    @GetMapping("/")
    fun getPages(
        @RequestParam(value = "page", defaultValue = "1") page: Int,
    ) {

    }
}

data class AuthorBriefDTO(
    val id: String,
    val name: String,
    val profileImageLink: String?,
)

data class RoleDTO(
    val id: String,
    val category: Category,
    val detail: String?,
    val headcount: String,
)