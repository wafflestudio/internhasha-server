package com.waffletoy.team1server.post.controller

import com.waffletoy.team1server.account.AuthUser
import com.waffletoy.team1server.account.AuthenticateException
import com.waffletoy.team1server.account.controller.User
import com.waffletoy.team1server.post.Category
import com.waffletoy.team1server.post.service.PostService
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/post")
class PostController(
    private val postService: PostService,
) {

    @GetMapping("/{post_id}")
    fun getPageDetail(
        @PathVariable("post_id") postId: String
    ):ResponseEntity<Post> {
        val post = postService.getPageDetail(postId)
        return ResponseEntity.ok(post)
    }

    @GetMapping("/")
    fun getPosts(
        @RequestParam(required = false) roles: List<String>?,
        @RequestParam(required = false) investment: Int?,
        @RequestParam(required = false) investors: List<String>?
    ): ResponseEntity<List<PostBrief>> {
        val posts = postService.getPosts(roles, investment, investors)
        return ResponseEntity.ok(posts)
    }

    @PostMapping("/{post_id}/bookmark")
    fun bookmarkPost(
        @Parameter(hidden = true) @AuthUser user: User?,
        @PathVariable("post_id") postId: String,
    ) : ResponseEntity<Void> {
        if (user == null) throw AuthenticateException("유효하지 않은 엑세스 토큰입니다.")
        postService.bookmarkPost(user, postId)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/{post_id}/bookmark")
    fun deleteBookmark(
        @Parameter(hidden = true) @AuthUser user: User?,
        @PathVariable("post_id") postId: String,
    ) : ResponseEntity<Void> {
        if (user == null) throw AuthenticateException("유효하지 않은 엑세스 토큰입니다.")
        postService.deleteBookmark(user, postId)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/post/bookmarks")
    fun getBookMarks(
        @Parameter(hidden = true) @AuthUser user: User?,
        @RequestParam(required = false) page: Int = 0,
    ): ResponseEntity<List<PostBrief>> {
        if (user == null) throw AuthenticateException("유효하지 않은 엑세스 토큰입니다.")
        val postList = postService.getBookmarks(user, page)
        return ResponseEntity.ok(postList)
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