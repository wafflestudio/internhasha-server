package com.wafflestudio.internhasha.s3.controller.dto

data class S3UploadResponse(
    val url: String,
    val s3Key: String,
)
