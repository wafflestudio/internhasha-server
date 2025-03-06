package com.waffletoy.team1server.s3.service

import com.amazonaws.HttpMethod
import com.amazonaws.SdkClientException
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.AmazonS3Exception
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.SSECustomerKey
import com.waffletoy.team1server.s3.S3FileType
import com.waffletoy.team1server.s3.S3SDKClientFailedException
import com.waffletoy.team1server.s3.S3UrlGenerationFailedException
import com.waffletoy.team1server.s3.controller.PreSignedDownloadReq
import com.waffletoy.team1server.s3.controller.PreSignedUploadReq
import com.waffletoy.team1server.user.dtos.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class S3Service(
    @Autowired
    private val amazonS3: AmazonS3,
    @Value("\${amazon.aws.bucketPublic}") private val bucketPublic: String,
    @Value("\${amazon.aws.bucketPrivate}") private val bucketPrivate: String,
    @Value("\${amazon.aws.bucketPrivateKey}") private val bucketPrivateKeyBase64: String,
) {
    // Lazy - 한 번만 파싱하고 이후 재사용하는 구조
    private val sseCustomerKey: SSECustomerKey by lazy {
        // Base64 디코딩, ByteArray를 String으로 변환
        val decodedKey = Base64.getDecoder().decode(bucketPrivateKeyBase64)
        val pemString = String(decodedKey).trim()
        // 실제 키 부분만 추출
        val rawKey =
            pemString
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("\n", "")
                .trim()
                .let { Base64.getDecoder().decode(it) }
        // S3 SSE-C용 Base64 키로 변환(인코딩)
        SSECustomerKey(Base64.getEncoder().encodeToString(rawKey))
    }

    fun generateUploadPreSignUrl(
        user: User,
        preSignedUploadReq: PreSignedUploadReq,
        expirationMinutes: Long = EXPIRATION_MINUTES,
    ): String {
        val (bucketName, isPrivate) =
            when (preSignedUploadReq.fileType) {
                S3FileType.CV, S3FileType.PORTFOLIO, S3FileType.IR_DECK, S3FileType.USER_THUMBNAIL -> bucketPrivate to true
                S3FileType.COMPANY_THUMBNAIL -> bucketPublic to false
            }

        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val randomString = UUID.randomUUID().toString().replace("-", "").take(10)
        val filePath = "static/${if (isPrivate) "private" else "public"}/${preSignedUploadReq.fileType}/${randomString}_$today/${preSignedUploadReq.fileName}"

        return try {
            val expiration = calculateExpiration(expirationMinutes)

            val presignedRequest =
                if (isPrivate) {
                    generateSseCPresignedUrl(bucketName, filePath, expiration, HttpMethod.PUT)
                } else {
                    amazonS3.generatePresignedUrl(bucketName, filePath, expiration, HttpMethod.PUT).toString()
                }

            presignedRequest
        } catch (e: AmazonS3Exception) {
            throw S3UrlGenerationFailedException()
        } catch (e: SdkClientException) {
            throw S3SDKClientFailedException()
        }
    }

    fun generateDownloadPreSignUrl(
        user: User,
        preSignedDownloadReq: PreSignedDownloadReq,
        expirationMinutes: Long = EXPIRATION_MINUTES,
    ): String {
        val (bucketName, isPrivate) =
            when (preSignedDownloadReq.fileType) {
                S3FileType.CV, S3FileType.PORTFOLIO, S3FileType.IR_DECK, S3FileType.USER_THUMBNAIL -> bucketPrivate to true
                S3FileType.COMPANY_THUMBNAIL -> bucketPublic to false
            }

        return try {
            val expiration = calculateExpiration(expirationMinutes)

            if (isPrivate) {
                generateSseCPresignedUrl(bucketName, preSignedDownloadReq.fileName, expiration, HttpMethod.GET)
            } else {
                amazonS3.generatePresignedUrl(bucketName, preSignedDownloadReq.fileName, expiration, HttpMethod.GET).toString()
            }
        } catch (e: AmazonS3Exception) {
            throw S3UrlGenerationFailedException()
        } catch (e: SdkClientException) {
            throw S3SDKClientFailedException()
        }
    }

    private fun generateSseCPresignedUrl(
        bucket: String,
        key: String,
        expiration: Date,
        method: HttpMethod,
    ): String {
        val request =
            GeneratePresignedUrlRequest(bucket, key)
                .withMethod(method)
                .withExpiration(expiration)
                .withSSECustomerKey(sseCustomerKey)

        return amazonS3.generatePresignedUrl(request).toString()
    }

    private fun calculateExpiration(expirationMinutes: Long): Date {
        return Date.from(Instant.now().plus(Duration.ofMinutes(expirationMinutes)))
    }

    companion object {
        const val EXPIRATION_MINUTES: Long = 10L
    }
}
