package com.waffletoy.internhasha.s3

import com.waffletoy.internhasha.exceptions.ApiException
import com.waffletoy.internhasha.exceptions.ErrorCode

class S3UrlGenerationFailedException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.S3_URL_GENERATION_FAILED,
    details = details,
)

class S3SDKClientFailedException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.S3_SDK_FAILED,
    details = details,
)

class S3CloudFrontKeyFailedException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.S3_CLOUDFRONT_KEY_FAILED,
    details = details,
)

class S3DeleteFailedException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.S3_SDK_FAILED,
    details = details,
)
