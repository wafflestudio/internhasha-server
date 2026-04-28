package com.wafflestudio.internhasha.s3.controller

import com.wafflestudio.internhasha.auth.security.AuthUser
import com.wafflestudio.internhasha.auth.security.AuthUserPrincipal
import com.wafflestudio.internhasha.s3.S3FileType
import com.wafflestudio.internhasha.s3.controller.dto.S3DownloadRequest
import com.wafflestudio.internhasha.s3.controller.dto.S3DownloadResponse
import com.wafflestudio.internhasha.s3.controller.dto.S3UploadRequest
import com.wafflestudio.internhasha.s3.controller.dto.S3UploadResponse
import com.wafflestudio.internhasha.s3.service.S3Service
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/s3")
@Validated
class S3Controller(
    private val s3Service: S3Service,
) {
    @PostMapping
    fun generateUploadUrl(
        @Parameter(hidden = true) @AuthUser user: AuthUserPrincipal,
        @Valid @RequestBody s3UploadRequest: S3UploadRequest,
    ): ResponseEntity<S3UploadResponse> {
        val (url, s3Key) = s3Service.generateUploadUrl(s3UploadRequest.toCommand())
        return ResponseEntity.ok(S3UploadResponse(url, s3Key))
    }

    @GetMapping
    fun generateDownloadUrl(
        @Parameter(hidden = true) @AuthUser user: AuthUserPrincipal,
        @RequestParam(required = true) s3Key: String,
        @RequestParam(required = true) fileType: S3FileType,
    ): ResponseEntity<S3DownloadResponse> {
        val s3DownloadRequest = S3DownloadRequest(fileType, s3Key)
        val url = s3Service.generateDownloadUrl(s3DownloadRequest.toCommand())
        return ResponseEntity.ok(S3DownloadResponse(url))
    }
}
