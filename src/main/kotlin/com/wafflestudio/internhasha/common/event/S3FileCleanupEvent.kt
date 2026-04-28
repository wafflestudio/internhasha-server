package com.wafflestudio.internhasha.common.event

import com.wafflestudio.internhasha.common.dto.S3Files

data class S3FileCleanupEvent(
    val s3Files: S3Files,
)
