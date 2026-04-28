package com.wafflestudio.internhasha.s3.controller.dto

import com.wafflestudio.internhasha.s3.S3FileType
import com.wafflestudio.internhasha.s3.service.dto.S3DownloadCommand
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class S3DownloadRequest(
    @field:NotNull(message = "파일 타입은 필수입니다.")
    val fileType: S3FileType,
    @field:Size(min = 1, max = 255, message = "must be between 1 and 255 characters.")
    @field:NotBlank(message = "파일 경로는 필수입니다.")
    val s3Key: String,
) {
    fun toCommand() =
        S3DownloadCommand(
            fileType = fileType,
            s3Key = s3Key,
        )
}
