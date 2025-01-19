package com.waffletoy.team1server.admin.service

import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.AmazonS3
import com.waffletoy.team1server.admin.controller.PreSignedDownloadReq
import com.waffletoy.team1server.admin.controller.PreSignedUploadReq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class S3Service(
    @Autowired
    private val amazonS3: AmazonS3? = null,
) {
    fun generateUploadPreSignUrl(
        preSignedUploadReq: PreSignedUploadReq,
        bucketName: String?,
        httpMethod: HttpMethod?,
    ): String {
        val filePath = "${preSignedUploadReq.fileName}.${preSignedUploadReq.fileType}"
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        // validfy of 10 minutes
        calendar.add(Calendar.MINUTE, 10)
        return amazonS3!!.generatePresignedUrl(bucketName, filePath, calendar.time, httpMethod).toString()
    }

    fun generateDownloadPreSignUrl(
        preSignedDownloadReq: PreSignedDownloadReq,
        bucketName: String?,
    ): String {
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        // Validity of 10 minutes
        calendar.add(Calendar.MINUTE, 10)
        return amazonS3!!.generatePresignedUrl(bucketName, preSignedDownloadReq.fileName, calendar.time, HttpMethod.GET).toString()
    }
}
