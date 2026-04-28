package com.wafflestudio.internhasha.post.controller

import com.wafflestudio.internhasha.auth.domain.RoleValue
import com.wafflestudio.internhasha.auth.security.AuthUser
import com.wafflestudio.internhasha.auth.security.AuthUserPrincipal
import com.wafflestudio.internhasha.post.controller.dto.response.Paginator
import com.wafflestudio.internhasha.post.controller.dto.response.PostBriefResponse
import com.wafflestudio.internhasha.post.controller.dto.response.PostBriefResponseWithPage
import com.wafflestudio.internhasha.post.service.BookmarkService
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.constraints.Min
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/post")
@Validated
class BookmarkController(
    private val bookmarkService: BookmarkService,
) {
    // 관심 채용 추가하기
    @PreAuthorize("hasAuthority('${RoleValue.ROLE_APPLICANT}')")
    @PostMapping("/{post_id}/bookmark")
    fun addBookmark(
        @Parameter(hidden = true) @AuthUser user: AuthUserPrincipal,
        @PathVariable("post_id") postId: String,
    ): ResponseEntity<Void> {
        bookmarkService.addBookmark(user.id, postId)
        return ResponseEntity.ok().build()
    }

    // 관심 채용 삭제하기
    @PreAuthorize("hasAuthority('${RoleValue.ROLE_APPLICANT}')")
    @DeleteMapping("/{post_id}/bookmark")
    fun deleteBookmark(
        @Parameter(hidden = true) @AuthUser user: AuthUserPrincipal,
        @PathVariable("post_id") postId: String,
    ): ResponseEntity<Void> {
        bookmarkService.deleteBookmark(user.id, postId)
        return ResponseEntity.ok().build()
    }

    // 북마크 가져오기
    @PreAuthorize("hasAuthority('${RoleValue.ROLE_APPLICANT}')")
    @GetMapping("/bookmarks")
    fun getBookMarks(
        @Parameter(hidden = true) @AuthUser user: AuthUserPrincipal,
        @RequestParam(required = false) @Min(0) page: Int?,
    ): ResponseEntity<PostBriefResponseWithPage> {
        val posts = bookmarkService.getBookmarks(user.id, page ?: 0)

        // 총 페이지
        val totalPages = posts.totalPages

        // PostBrief로 매핑하여 반환
        return ResponseEntity.ok(
            PostBriefResponseWithPage(
                posts = posts.content.map { PostBriefResponse.fromPost(it) },
                paginator = Paginator(totalPages),
            ),
        )
    }
}
