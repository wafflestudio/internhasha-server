package com.wafflestudio.internhasha.s3.service.dto

import com.wafflestudio.internhasha.s3.S3FileType

data class S3DownloadCommand(
    val fileType: S3FileType,
    val s3Key: String,
)
