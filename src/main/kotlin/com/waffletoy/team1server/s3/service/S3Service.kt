package com.waffletoy.team1server.s3.service

import com.amazonaws.HttpMethod
import com.amazonaws.SdkClientException
import com.amazonaws.services.cloudfront.CloudFrontUrlSigner
import com.amazonaws.services.cloudfront.util.SignerUtils
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
import java.io.File
import java.net.URLEncoder
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
    @Value("\${cloudfront.privateKeyPath}") private val privateKeyPath: String,
) {
    // Lazy - 한 번만 파싱하고 이후 재사용하는 구조
    private val tempPrivateKeyFile: File by lazy {
        createPrivateKeyFile()
    }

    private fun createPrivateKeyFile(): File {
        val decodedKey = Base64.getDecoder().decode(privateKeyText) // Base64 디코딩

        return File.createTempFile("temp-private-key", ".pem").apply {
            writeBytes(decodedKey) // 디코딩된 키를 파일로 저장
            deleteOnExit() // 애플리케이션 종료 시 삭제
        }
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
//        val privateKeyStream = ClassPathResource(privateKeyPath).inputStream
//        val tempFile =
//            File.createTempFile("temp-key", ".pem").apply {
//                writeText(privateKeyStream.bufferedReader().use { it.readText() })
//            }

        return CloudFrontUrlSigner.getSignedURLWithCannedPolicy(
            SignerUtils.Protocol.https,
            domainName,
            tempPrivateKeyFile,
            filePath,
            keyPairId,
            expiration,
        )
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
