package com.wafflestudio.internhasha.s3.controller

import com.wafflestudio.internhasha.s3.S3FileType
import com.wafflestudio.internhasha.s3.controller.dto.S3DownloadRequest
import com.wafflestudio.internhasha.s3.controller.dto.S3DownloadResponse
import com.wafflestudio.internhasha.s3.controller.dto.S3UploadRequest
import com.wafflestudio.internhasha.s3.controller.dto.S3UploadResponse
import com.wafflestudio.internhasha.s3.service.S3Service
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/s3")
@Validated
class S3Controller(
    private val s3Service: S3Service,
) {
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    fun generateUploadUrl(
        @Valid @RequestBody s3UploadRequest: S3UploadRequest,
    ): ResponseEntity<S3UploadResponse> {
        val (url, s3Key) = s3Service.generateUploadUrl(s3UploadRequest.toCommand())
        return ResponseEntity.ok(S3UploadResponse(url, s3Key))
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    fun generateDownloadUrl(
        @RequestParam(required = true) s3Key: String,
        @RequestParam(required = true) fileType: S3FileType,
    ): ResponseEntity<S3DownloadResponse> {
        val s3DownloadRequest = S3DownloadRequest(fileType, s3Key)
        val url = s3Service.generateDownloadUrl(s3DownloadRequest.toCommand())
        return ResponseEntity.ok(S3DownloadResponse(url))
    }
}
