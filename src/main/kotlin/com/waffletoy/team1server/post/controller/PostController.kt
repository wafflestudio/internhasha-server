package com.waffletoy.team1server.post.controller

import com.waffletoy.team1server.auth.AuthUser
import com.waffletoy.team1server.auth.AuthUserOrNull
import com.waffletoy.team1server.auth.dto.User
import com.waffletoy.team1server.post.dto.*
import com.waffletoy.team1server.post.service.PostService
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/post")
@Validated
class PostController(
    private val postService: PostService,
) {
    // 채용 공고 상세 페이지 불러오기
    @GetMapping("/{post_id}")
    fun getPageDetail(
        // User 토큰이 들어올 수도, 아닐 수도 있음
        @Parameter(hidden = true) @AuthUserOrNull user: User?,
        @PathVariable("post_id") postId: String,
    ): ResponseEntity<Post> {
        val post = postService.getPageDetail(user, postId)
        return ResponseEntity.ok(post)
    }

    // 채용 공고 리스트 불러오기
    @GetMapping
    fun getPosts(
        // User 토큰이 들어올 수도, 아닐 수도 있음
        @Parameter(hidden = true) @AuthUserOrNull user: User?,
        @RequestParam(required = false) roles: List<String>?,
        @RequestParam(required = false) status: Boolean?,
        @RequestParam(required = false) domains: List<String>?,
        @RequestParam(required = false) @Min(0) page: Int?,
        @RequestParam(required = false) @Min(0) @Max(1) order: Int?,
    ): ResponseEntity<PostWithPage> {
        val posts =
            postService.getPosts(
                user = user,
                positions = roles,
                page = page ?: 0,
                order = order ?: 0,
                isActive = status,
                domains = domains,
            )

        // 총 페이지
        val totalPages = posts.totalPages

        // PostBrief로 매핑하여 반환
        return ResponseEntity.ok(
            PostWithPage(
                posts = posts.content.map { PostBrief.fromPost(it) },
                paginator = Paginator(totalPages),
            ),
        )
    }

    // 관심 채용 추가하기
    @PostMapping("/{post_id}/bookmark")
    fun addBookmark(
        @Parameter(hidden = true) @AuthUser user: User,
        @PathVariable("post_id") postId: String,
    ): ResponseEntity<Void> {
        postService.addBookmark(user.id, postId)
        return ResponseEntity.ok().build()
    }

    // 관심 채용 삭제하기
    @DeleteMapping("/{post_id}/bookmark")
    fun deleteBookmark(
        @Parameter(hidden = true) @AuthUser user: User,
        @PathVariable("post_id") postId: String,
    ): ResponseEntity<Void> {
        postService.deleteBookmark(user.id, postId)
        return ResponseEntity.ok().build()
    }

    // 북마크 가져오기
    @GetMapping("/bookmarks")
    fun getBookMarks(
        @Parameter(hidden = true) @AuthUser user: User,
        @RequestParam(required = false) @Min(0) page: Int?,
    ): ResponseEntity<PostWithPage> {
        val posts = postService.getBookmarks(user.id, page ?: 0)

        // 총 페이지
        val totalPages = posts.totalPages

        // PostBrief로 매핑하여 반환
        return ResponseEntity.ok(
            PostWithPage(
                posts = posts.content.map { PostBrief.fromPost(it) },
                paginator = Paginator(totalPages),
            ),
        )
    }

    // dev
    @PostMapping("/dev/make-dummy")
    fun makeDummyPost(
        @RequestBody pw: PasswordRequest,
    ): ResponseEntity<Void> {
        postService.makeDummyPosts(3, pw.pw)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/position")
    fun createPosition(
        @Parameter(hidden = true) @AuthUser user: User,
        @Valid @RequestBody request: CreatePositionRequest,
    ): ResponseEntity<Position> {
        val position = postService.createPosition(user, request)
        return ResponseEntity.ok(position)
    }

    @PutMapping("/position/{position_id}")
    fun updatePosition(
        @Parameter(hidden = true) @AuthUser user: User,
            @PathVariable("position_id") positionId: String,
        @Valid @RequestBody request: UpdatePositionRequest,
    ): ResponseEntity<Position> {
        val position = postService.updatePosition(user, positionId, request)
        return ResponseEntity.ok(position)
    }

    @DeleteMapping("/position/{position_id}")
    fun deletePosition(
        @Parameter(hidden = true) @AuthUser user: User,
        @PathVariable("position_id") positionId: String,
    ): ResponseEntity<Void> {
        val position = postService.deletePosition(user, positionId)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/position/me")
    fun getPostByCompany(
        @Parameter(hidden = true) @AuthUser user: User,
        @RequestParam(required = false) roles: List<String>?,
        @RequestParam(required = false) status: Boolean?,
        @RequestParam(required = false) domains: List<String>?,
        @RequestParam(required = false) @Min(0) page: Int?,
        @RequestParam(required = false) @Min(0) @Max(1) order: Int?,
    ): ResponseEntity<PostWithPage> {
        val posts =
            postService.getPostByCompany(
                user = user,
                positions = roles,
                page = page ?: 0,
                order = order ?: 0,
                isActive = status,
                domains = domains,
            )

        // 총 페이지
        val totalPages = posts.totalPages

        // PostBrief로 매핑하여 반환
        return ResponseEntity.ok(
            PostWithPage(
                posts = posts.content.map { PostBrief.fromPost(it) },
                paginator = Paginator(totalPages),
            ),
        )
    }

    // 공고 즉시 마감
    @PatchMapping("position/{position_id}/close")
    fun closePosition(
        @Parameter(hidden = true) @AuthUser user: User,
        @PathVariable("position_id") positionId: String,
    ): ResponseEntity<Void> {
        postService.closePosition(user, positionId)
        return ResponseEntity.noContent().build()
    }
}

data class Paginator(
    val lastPage: Int,
)

data class PostWithPage(
    val posts: List<PostBrief>,
    val paginator: Paginator,
)

data class PasswordRequest(val pw: String)
