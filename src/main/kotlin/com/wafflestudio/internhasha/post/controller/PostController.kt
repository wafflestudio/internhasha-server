package com.wafflestudio.internhasha.post.controller

import com.wafflestudio.internhasha.auth.domain.RoleValue
import com.wafflestudio.internhasha.auth.security.AuthUser
import com.wafflestudio.internhasha.auth.security.AuthUserPrincipal
import com.wafflestudio.internhasha.post.controller.dto.request.CreatePositionRequest
import com.wafflestudio.internhasha.post.controller.dto.request.UpdatePositionRequest
import com.wafflestudio.internhasha.post.controller.dto.response.Paginator
import com.wafflestudio.internhasha.post.controller.dto.response.PositionResponse
import com.wafflestudio.internhasha.post.controller.dto.response.PostBriefResponse
import com.wafflestudio.internhasha.post.controller.dto.response.PostBriefResponseWithPage
import com.wafflestudio.internhasha.post.controller.dto.response.PostResponse
import com.wafflestudio.internhasha.post.service.PostService
import com.wafflestudio.internhasha.post.service.dto.PositionSearchCond
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
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
        @Parameter(hidden = true) @AuthUser user: AuthUserPrincipal?,
        @PathVariable("post_id") postId: String,
    ): ResponseEntity<PostResponse> {
        val post = postService.getPageDetail(user, postId)
        return ResponseEntity.ok(PostResponse.fromDto(post))
    }

    // 채용 공고 리스트 불러오기
    @GetMapping
    fun getPosts(
        // User 토큰이 들어올 수도, 아닐 수도 있음
        @Parameter(hidden = true) @AuthUser user: AuthUserPrincipal?,
        @RequestParam(required = false) roles: List<String>?,
        @RequestParam(required = false) isActive: Boolean?,
        @RequestParam(required = false) domains: List<String>?,
        @RequestParam(required = false) @Min(0) page: Int?,
        @RequestParam(required = false) @Min(0) @Max(1) order: Int?,
    ): ResponseEntity<PostBriefResponseWithPage> {
        val cond =
            PositionSearchCond(
                positions = roles,
                order = order ?: 0,
                companyId = null,
                isActive = isActive ?: false,
                domains = domains,
            )
        val posts =
            postService.getPosts(
                authUser = user,
                cond = cond,
                page = page ?: 0,
            )

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

    @PreAuthorize("hasAuthority('${RoleValue.ROLE_COMPANY}')")
    @PostMapping("/position")
    fun createPosition(
        @Parameter(hidden = true) @AuthUser user: AuthUserPrincipal,
        @Valid @RequestBody request: CreatePositionRequest,
    ): ResponseEntity<PositionResponse> {
        val position = postService.createPosition(request.toCommand(user.id))
        return ResponseEntity.ok(PositionResponse.fromDto(position))
    }

    @PreAuthorize("hasAuthority('${RoleValue.ROLE_COMPANY}')")
    @PutMapping("/position/{position_id}")
    fun updatePosition(
        @Parameter(hidden = true) @AuthUser user: AuthUserPrincipal,
        @PathVariable("position_id") positionId: String,
        @Valid @RequestBody request: UpdatePositionRequest,
    ): ResponseEntity<PositionResponse> {
        val position = postService.updatePosition(positionId, request.toCommand(user.id))
        return ResponseEntity.ok(PositionResponse.fromDto(position))
    }

    @Deprecated("삭제 대신 마감 기능")
    @PreAuthorize("hasAuthority('${RoleValue.ROLE_COMPANY}')")
    @DeleteMapping("/position/{position_id}")
    fun deletePosition(
        @PathVariable("position_id") positionId: String,
    ): ResponseEntity<Void> {
        // val position = postService.deletePosition(user, positionId)
        return ResponseEntity.ok().build()
    }

    @PreAuthorize("hasAuthority('${RoleValue.ROLE_COMPANY}')")
    @GetMapping("/position/me")
    fun getPostByCompany(
        @Parameter(hidden = true) @AuthUser user: AuthUserPrincipal,
        @RequestParam(required = false) roles: List<String>?,
        @RequestParam(required = false) isActive: Boolean?,
        @RequestParam(required = false) domains: List<String>?,
        @RequestParam(required = false) @Min(0) page: Int?,
        @RequestParam(required = false) @Min(0) @Max(1) order: Int?,
    ): ResponseEntity<PostBriefResponseWithPage> {
        val cond =
            PositionSearchCond(
                positions = roles,
                order = order ?: 0,
                companyId = null,
                isActive = isActive ?: false,
                domains = domains,
            )

        val posts =
            postService.getPostByCompany(
                authUser = user,
                cond = cond,
                page = page ?: 0,
            )

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

    // 공고 즉시 마감
    @PreAuthorize("hasAuthority('${RoleValue.ROLE_COMPANY}')")
    @PatchMapping("position/{position_id}/close")
    fun closePosition(
        @Parameter(hidden = true) @AuthUser user: AuthUserPrincipal,
        @PathVariable("position_id") positionId: String,
    ): ResponseEntity<Void> {
        postService.closePosition(user, positionId)
        return ResponseEntity.ok().build()
    }
}
