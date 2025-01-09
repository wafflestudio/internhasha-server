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
    // 채용 공고 상세 페이지 불러오기
    @GetMapping("/{post_id}")
    fun getPageDetail(
        @PathVariable("post_id") postId: String,
    ): ResponseEntity<Post> {
        val post = postService.getPageDetail(postId)
        return ResponseEntity.ok(post)
    }

    // 채용 공고 리스트 불러오기
    @GetMapping
    fun getPosts(
        @RequestParam(required = false) roles: List<String>?,
        @RequestParam(required = false) investment: Int?,
        @RequestParam(required = false) investors: List<String>?,
        @RequestParam(required = false) status: Int?,
        @RequestParam(required = false) page: Int?,
    ): ResponseEntity<PostWithPageDTO> {
        val posts = postService.getPosts(roles, investment, investors, status, page ?: 0)

        // 총 페이지
        val totalPages = posts.totalPages

        // PostBrief로 매핑하여 반환
        return ResponseEntity.ok(
            PostWithPageDTO(
                posts = posts.content.map { PostBrief.fromPost(Post.fromEntity(it)) },
                paginator = Paginator(totalPages),
            ),
        )
    }

    // 관심 채용 추가하기
    @PostMapping("/{post_id}/bookmark")
    fun bookmarkPost(
        @Parameter(hidden = true) @AuthUser user: User?,
        @PathVariable("post_id") postId: String,
    ): ResponseEntity<Void> {
        if (user == null) throw AuthenticateException("유효하지 않은 엑세스 토큰입니다.")
        postService.bookmarkPost(user, postId)
        return ResponseEntity.ok().build()
    }

    // 관심 채용 삭제하기
    @DeleteMapping("/{post_id}/bookmark")
    fun deleteBookmark(
        @Parameter(hidden = true) @AuthUser user: User?,
        @PathVariable("post_id") postId: String,
    ): ResponseEntity<Void> {
        if (user == null) throw AuthenticateException("유효하지 않은 엑세스 토큰입니다.")
        postService.deleteBookmark(user, postId)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/bookmarks")
    fun getBookMarks(
        @Parameter(hidden = true) @AuthUser user: User?,
        @RequestParam(required = false) page: Int?,
    ): ResponseEntity<PostWithPageDTO> {
        if (user == null) throw AuthenticateException("유효하지 않은 엑세스 토큰입니다.")
        val posts = postService.getBookmarks(user, page ?: 0)

        // 총 페이지
        val totalPages = posts.totalPages

        // PostBrief로 매핑하여 반환
        return ResponseEntity.ok(
            PostWithPageDTO(
                posts = posts.content.map { PostBrief.fromPost(Post.fromEntity(it)) },
                paginator = Paginator(totalPages),
            ),
        )
    }

    @PostMapping("/make-dummy")
    fun makeDummyPost(
        @RequestBody cnt: Int,
    ) {
        postService.makeDummyPosts(cnt)
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

data class Paginator(
    val lastPage: Int,
)

data class PostWithPageDTO(
    val posts: List<PostBrief>,
    val paginator: Paginator,
)
