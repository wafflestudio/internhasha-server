package com.wafflestudio.internhasha.s3

import com.wafflestudio.internhasha.exceptions.ApiException
import com.wafflestudio.internhasha.exceptions.ErrorCode

class S3UrlGenerationFailedException(
    ex: Exception,
    details: Map<String, Any>? = null,
) : ApiException(
        errorCode = ErrorCode.S3_URL_GENERATION_FAILED,
        details = details,
        ex = ex,
    )

class S3SDKClientFailedException(
    ex: Exception,
    details: Map<String, Any>? = null,
) : ApiException(
        errorCode = ErrorCode.S3_SDK_FAILED,
        details = details,
        ex = ex,
    )

class S3CloudFrontKeyFailedException(
    ex: Exception,
    details: Map<String, Any>? = null,
) : ApiException(
        errorCode = ErrorCode.S3_CLOUDFRONT_KEY_FAILED,
        details = details,
        ex = ex,
    )

class S3DeleteFailedException(
    ex: Exception,
    details: Map<String, Any>? = null,
) : ApiException(
        errorCode = ErrorCode.S3_SDK_FAILED,
        details = details,
        ex = ex,
    )
