package com.waffletoy.team1server.post.service

import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.AmazonS3
import com.waffletoy.team1server.post.controller.PreSignedDownloadReq
import com.waffletoy.team1server.post.controller.PreSignedUploadReq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.*

@Service
class S3Service(
    @Autowired
    private val amazonS3: AmazonS3,
) {
    fun generateUploadPreSignUrl(
        preSignedUploadReq: PreSignedUploadReq,
        bucketName: String,
        expirationMinutes: Long = EXPIRATION_MINUTES,
    ): String {
        val filePath = "${preSignedUploadReq.fileName}.${preSignedUploadReq.fileType}"
        val expiration = calculateExpiration(expirationMinutes)
        return amazonS3.generatePresignedUrl(bucketName, filePath, expiration, HttpMethod.PUT).toString()
    }

    fun generateDownloadPreSignUrl(
        preSignedDownloadReq: PreSignedDownloadReq,
        bucketName: String,
        expirationMinutes: Long = EXPIRATION_MINUTES,
    ): String {
        val expiration = calculateExpiration(expirationMinutes)
        return amazonS3.generatePresignedUrl(bucketName, preSignedDownloadReq.fileName, expiration, HttpMethod.GET).toString()
    }

    private fun calculateExpiration(expirationMinutes: Long): Date {
        return Date.from(Instant.now().plus(Duration.ofMinutes(expirationMinutes)))
    }

    companion object {
        const val EXPIRATION_MINUTES: Long = 10L
    }
}
