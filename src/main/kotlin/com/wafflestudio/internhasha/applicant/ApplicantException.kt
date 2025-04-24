package com.wafflestudio.internhasha.applicant

import com.wafflestudio.internhasha.exceptions.ApiException
import com.wafflestudio.internhasha.exceptions.ErrorCode

class ApplicantUserForbiddenException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.APPLICANT_USER_FORBIDDEN,
    details = details,
)

class ApplicantNotFoundException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.APPLICANT_NOT_FOUND,
    details = details,
)
