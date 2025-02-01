package com.waffletoy.team1server.post.controller

import com.waffletoy.team1server.post.dto.*
import com.waffletoy.team1server.post.service.PostService
import com.waffletoy.team1server.post.service.S3Service
import com.waffletoy.team1server.user.AuthUser
import com.waffletoy.team1server.user.AuthUserOrNull
import com.waffletoy.team1server.user.dtos.User
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/post")
@Validated
class PostController(
    private val postService: PostService,
    private val s3Service: S3Service,
    @Value("\${amazon.aws.bucket}") private val bucketName: String,
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
        @RequestParam(required = false) @Min(0) investmentMax: Int?,
        @RequestParam(required = false) @Min(0) investmentMin: Int?,
        @RequestParam(required = false) @Min(0) @Max(2) status: Int?,
        @RequestParam(required = false) series: List<String>?,
        @RequestParam(required = false) @Min(0) page: Int?,
        @RequestParam(required = false) @Min(0) @Max(1) order: Int?,
    ): ResponseEntity<PostWithPage> {
        val posts = postService.getPosts(user, roles, investmentMax, investmentMin, status, series, page ?: 0, order ?: 0)

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

    // s3 bucket
    @PostMapping("/upload/presigned")
    fun generateUploadPresignedUrl(
        @Parameter(hidden = true) @AuthUser user: User,
        @RequestBody preSignedUploadReq: PreSignedUploadReq,
    ): ResponseEntity<PresignedURL> {
        val presignedUrl = s3Service.generateUploadPreSignUrl(user, preSignedUploadReq, bucketName)
        return ResponseEntity.ok(PresignedURL(presignedUrl))
    }

    @PostMapping("/download/presigned")
    fun generateDownloadPresignedUrl(
        @Parameter(hidden = true) @AuthUser user: User,
        @RequestBody preSignedDownloadReq: PreSignedDownloadReq,
    ): ResponseEntity<PresignedURL> {
        val presignedUrl = s3Service.generateDownloadPreSignUrl(user, preSignedDownloadReq, bucketName)
        return ResponseEntity.ok(PresignedURL(presignedUrl))
    }

    // dev
    @PostMapping("/dev/make-dummy")
    fun makeDummyPost(
        @RequestBody cnt: Int,
        @RequestBody pw: PasswordRequest,
    ): ResponseEntity<Void> {
        postService.makeDummyPosts(cnt, pw.pw)
        return ResponseEntity.ok().build()
    }

//    @PostMapping("/dev/reset-db")
//    fun resetDBPosts(
//        @RequestBody pw: PasswordRequest,
//    ): ResponseEntity<Void> {
//        if (pw.pw == "0000") {
//            postService.resetDB(pw.pw)
//            return ResponseEntity.ok().build()
//        } else {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
//        }
//    }

    @PostMapping("/company")
    fun createCompany(
        @Parameter(hidden = true) @AuthUser user: User,
        @Valid @RequestBody request: CreateCompanyRequest,
    ): ResponseEntity<Company> {
        val company = postService.createCompany(user, request)
        return ResponseEntity.ok(company)
    }

    @PostMapping("/company/{company_id}/position")
    fun createPosition(
        @Parameter(hidden = true) @AuthUser user: User,
        @PathVariable("company_id") companyId: String,
        @Valid @RequestBody request: CreatePositionRequest,
    ): ResponseEntity<Position> {
        val position = postService.createPosition(user, companyId, request)
        return ResponseEntity.ok(position)
    }

    @GetMapping("/company/me")
    fun getCompanyByCurator(
        @Parameter(hidden = true) @AuthUser user: User,
    ): ResponseEntity<List<Company>> {
        val companies = postService.getCompanyByCurator(user)
        return ResponseEntity.ok(companies)
    }

    @GetMapping("/position/me")
    fun getPostByCurator(
        @Parameter(hidden = true) @AuthUser user: User,
        @RequestParam(required = false) roles: List<String>?,
        @RequestParam(required = false) @Min(0) investmentMax: Int?,
        @RequestParam(required = false) @Min(0) investmentMin: Int?,
        @RequestParam(required = false) @Min(0) @Max(2) status: Int?,
        @RequestParam(required = false) series: List<String>?,
        @RequestParam(required = false) @Min(0) page: Int?,
        @RequestParam(required = false) @Min(0) @Max(1) order: Int?,
    ): ResponseEntity<PostWithPage> {
        val posts = postService.getPostByCurator(user, roles, investmentMax, investmentMin, status, series, page ?: 0, order ?: 0)

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
}

data class Paginator(
    val lastPage: Int,
)

data class PostWithPage(
    val posts: List<PostBrief>,
    val paginator: Paginator,
)

data class PreSignedUploadReq(
    val fileName: String,
    val fileType: String,
)

data class PreSignedDownloadReq(
    val fileName: String,
)

data class PresignedURL(
    val presignedUrl: String,
)

data class PasswordRequest(val pw: String)
