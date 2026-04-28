package com.wafflestudio.internhasha.s3.service.dto

import com.wafflestudio.internhasha.s3.S3FileType

data class S3UploadCommand(
    val fileName: String,
    val fileType: S3FileType,
)
