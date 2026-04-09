package com.wafflestudio.internhasha.common.event

import com.wafflestudio.internhasha.s3.dto.S3Files

data class S3FileCleanupEvent(
    val s3Files: S3Files,
)
