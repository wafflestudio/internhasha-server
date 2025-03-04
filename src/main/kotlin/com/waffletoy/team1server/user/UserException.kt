package com.waffletoy.team1server.user

import com.waffletoy.team1server.exceptions.ApiException
import com.waffletoy.team1server.exceptions.ErrorCode

class UserDuplicateSnuMailException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.USER_DUPLICATE_SNUMAIL,
    details = details,
)

class UserDuplicateLocalIdException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.USER_DUPLICATE_LOCAL_ID,
    details = details,
)

class UserNotFoundException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.USER_NOT_FOUND,
    details = details,
)
class UserEmailVerificationInvalidException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.USER_EMAIL_VERIFICATION_INVALID,
    details = details,
)
