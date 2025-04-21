package com.waffletoy.internhasha.exceptions

open class ApiException(
    val errorCode: ErrorCode,
    override val message: String = errorCode.description,
    val details: Map<String, Any>? = null,
) : RuntimeException(message)

class BadAuthorizationHeaderException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.AUTH_BAD_HEADER,
    details = details,
)

class InvalidCredentialsException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.AUTH_INVALID_CREDENTIALS,
    details = details,
)

class InvalidRefreshTokenException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.AUTH_INVALID_REFRESH_TOKEN,
    details = details,
)

class InvalidAccessTokenException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.AUTH_INVALID_ACCESS_TOKEN,
    details = details,
)

class TokenMismatchException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.AUTH_TOKEN_MISMATCH,
    details = details,
)

class OAuthFailedException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.AUTH_OAUTH_FAILURE,
    details = details,
)

class NotAuthorizedException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.AUTH_NOT_AUTHORIZED,
    details = details,
)

class NotImplementedException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.NOT_IMPLEMENTED,
    details = details,
)

class InvalidRequestException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.INVALID_REQUEST,
    details = details,
)
