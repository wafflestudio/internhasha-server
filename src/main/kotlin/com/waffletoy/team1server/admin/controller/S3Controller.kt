package com.waffletoy.team1server.admin.controller

import com.amazonaws.HttpMethod
import com.waffletoy.team1server.admin.service.S3Service
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin")
class FileUploadController(
    private val s3Service: S3Service,
    @Value("\${amazon.aws.bucket}") private val bucketName: String,
) {
    @PostMapping("/post/upload/presigned")
    fun generateUploadPresignedUrl(
        @RequestBody preSignedUploadReq: PreSignedUploadReq,
    ): ResponseEntity<PresignedURL> {
        val presignedUrl = s3Service.generateUploadPreSignUrl(preSignedUploadReq, bucketName, HttpMethod.PUT)
        return ResponseEntity.ok(PresignedURL(presignedUrl))
    }

    @PostMapping("/post/download/presigned")
    fun generateDownloadPresignedUrl(
        @RequestBody preSignedDownloadReq: PreSignedDownloadReq,
    ): ResponseEntity<PresignedURL> {
        val presignedUrl = s3Service.generateDownloadPreSignUrl(preSignedDownloadReq, bucketName)
        return ResponseEntity.ok(PresignedURL(presignedUrl))
    }
}

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
