package com.waffletoy.internhasha.applicant

import com.waffletoy.internhasha.exceptions.ApiException
import com.waffletoy.internhasha.exceptions.ErrorCode

class ApplicantUserForbiddenException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.APPLICANT_USER_FORBIDDEN,
    details = details,
)

class ApplicantNotFoundException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.APPLICANT_NOT_FOUND,
    details = details,
)
