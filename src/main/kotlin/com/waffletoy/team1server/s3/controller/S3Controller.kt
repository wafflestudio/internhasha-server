package com.waffletoy.team1server.s3.controller

import com.waffletoy.team1server.s3.S3FileType
import com.waffletoy.team1server.s3.service.S3Service
import com.waffletoy.team1server.user.AuthUser
import com.waffletoy.team1server.user.dtos.User
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/s3")
@Validated
class S3Controller(
    private val s3Service: S3Service,
) {
    // s3 bucket
    @PostMapping("/upload/presigned")
    fun generateUploadPresignedUrl(
        @Parameter(hidden = true) @AuthUser user: User,
        @RequestBody preSignedUploadReq: PreSignedUploadReq,
    ): ResponseEntity<PresignedURL> {
        val presignedUrl = s3Service.generateUploadPreSignUrl(user, preSignedUploadReq)
        return ResponseEntity.ok(PresignedURL(presignedUrl))
    }

    @PostMapping("/download/presigned")
    fun generateDownloadPresignedUrl(
        @Parameter(hidden = true) @AuthUser user: User,
        @RequestBody preSignedDownloadReq: PreSignedDownloadReq,
    ): ResponseEntity<PresignedURL> {
        val presignedUrl = s3Service.generateDownloadPreSignUrl(user, preSignedDownloadReq)
        return ResponseEntity.ok(PresignedURL(presignedUrl))
    }
}

data class PreSignedUploadReq(
    val fileName: String,
    val fileType: S3FileType,
)

data class PreSignedDownloadReq(
    val fileType: S3FileType,
    val fileName: String,
)

data class PresignedURL(
    val presignedUrl: String,
)
