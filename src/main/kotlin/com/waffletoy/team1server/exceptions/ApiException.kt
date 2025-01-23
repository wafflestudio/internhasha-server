package com.waffletoy.team1server.exceptions

open class ApiException(
    val errorCode: ErrorCode,
    override val message: String = errorCode.description,
    val details: Map<String, Any>? = null,
) : RuntimeException(message)

class UserDuplicateSnuMailException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.USER_DUPLICATE_SNUMAIL,
    details = details,
)

class UserDuplicateLocalIdException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.USER_DUPLICATE_LOCAL_ID,
    details = details,
)

class UserDuplicateGoogleIdException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.USER_DUPLICATE_GOOGLE_ID,
    details = details,
)

class UserNotFoundException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.USER_NOT_FOUND,
    details = details,
)

class UserRoleConflictException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.USER_INVALID_ROLE,
    details = details,
)

class UserMergeUnknownFailureException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.USER_MERGE_UNKNOWN_FAILURE,
    details = details,
)

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

class EmailVerificationFailedException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.EMAIL_VERIFICATION_FAILED,
    details = details,
)

class EmailVerificationInvalidException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.EMAIL_VERIFICATION_INVALID,
    details = details,
)

class EmailVerificationSendFailureException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.EMAIL_VERIFICATION_SEND_FAILURE,
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
