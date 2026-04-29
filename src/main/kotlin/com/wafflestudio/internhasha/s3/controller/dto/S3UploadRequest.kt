package com.wafflestudio.internhasha.s3.controller.dto

import com.wafflestudio.internhasha.s3.S3FileType
import com.wafflestudio.internhasha.s3.service.dto.S3UploadCommand
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class S3UploadRequest(
    @field:NotBlank(message = "파일 이름은 필수입니다.")
    @field:Size(min = 1, max = 255, message = "File name must be between 1 and 255 characters.")
    val fileName: String,
    val fileType: S3FileType,
) {
    fun toCommand() =
        S3UploadCommand(
            fileName = fileName,
            fileType = fileType,
        )
}
