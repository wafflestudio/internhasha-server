package com.waffletoy.team1server.s3.service

import com.amazonaws.HttpMethod
import com.amazonaws.SdkClientException
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.AmazonS3Exception
import com.waffletoy.team1server.s3.S3FileType
import com.waffletoy.team1server.s3.S3SDKClientFailedException
import com.waffletoy.team1server.s3.S3UrlGenerationFailedException
import com.waffletoy.team1server.s3.controller.PreSignedDownloadReq
import com.waffletoy.team1server.s3.controller.PreSignedUploadReq
import com.waffletoy.team1server.user.dtos.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URLEncoder
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
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
    @Value("\${cloudfront.keyPairId}") private val keyPairId: String,
    @Value("\${cloudfront.privateKeyText}") private val privateKeyText: String,
    @Value("\${custom.domain-name}") private val domainName: String,
) {
    // Lazy - 한 번만 파싱하고 이후 재사용하는 구조
    private val privateKey: PrivateKey by lazy {
        val keyFactory = KeyFactory.getInstance("RSA")
        val keyContent =
            privateKeyText
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\\s".toRegex(), "")

        val keyBytes = Base64.getDecoder().decode(keyContent)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)

        keyFactory.generatePrivate(keySpec)
    }

    // 업로드는 presigned url 사용
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
        val expiration = calculateExpiration(expirationMinutes)

        return generateS3PresignedUrl(bucketName, filePath, expiration, HttpMethod.PUT)
    }

    // 다운로드는 공개 url & signed url
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
        val expiration = calculateExpiration(expirationMinutes)

        return if (isPrivate) {
            generateCloudfrontSignedUrl(preSignedDownloadReq.fileName, expiration)
        } else {
            generateS3PresignedUrl(bucketName, preSignedDownloadReq.fileName, expiration, HttpMethod.GET)
        }
    }

    private fun generateS3PresignedUrl(
        bucketName: String,
        filePath: String,
        expiration: Date,
        httpMethod: HttpMethod,
    ): String {
        try {
            return amazonS3.generatePresignedUrl(bucketName, filePath, expiration, httpMethod).toString()
        } catch (e: AmazonS3Exception) {
            throw S3UrlGenerationFailedException()
        } catch (e: SdkClientException) {
            throw S3SDKClientFailedException()
        }
    }

    private fun generateCloudfrontSignedUrl(
        filePath: String,
        expiration: Date,
    ): String {
        val resourcePath = "$domainName/$filePath"
        val expiresAt = expiration.toInstant().epochSecond

        // 서명할 문자열은 "<resource>?Expires=<expiration>" 형식
        val stringToSign = "$resourcePath?Expires=$expiresAt&Key-Pair-Id=$keyPairId"

        // RSA 서명 생성
        val signatureInstance =
            Signature.getInstance("SHA256withRSA").apply {
                initSign(privateKey)
                update(stringToSign.toByteArray())
            }
        val signedBytes = signatureInstance.sign()

        // Base64 URL-safe 인코딩
        val encodedSignature = Base64.getUrlEncoder().encodeToString(signedBytes)

        return "$stringToSign&Signature=$encodedSignature"
    }

    private fun urlEncode(value: String): String {
        return URLEncoder.encode(value, "UTF-8")
    }

    private fun calculateExpiration(expirationMinutes: Long): Date {
        return Date.from(Instant.now().plus(Duration.ofMinutes(expirationMinutes)))
    }

    companion object {
        const val EXPIRATION_MINUTES: Long = 10L
    }
}
