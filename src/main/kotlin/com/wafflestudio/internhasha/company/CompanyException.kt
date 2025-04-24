package com.wafflestudio.internhasha.company

import com.wafflestudio.internhasha.exceptions.ApiException
import com.wafflestudio.internhasha.exceptions.ErrorCode

class CompanyNotFoundException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.COMPANY_NOT_FOUND,
    details = details,
)

class CompanyCreationFailedException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.COMPANY_CREATION_FAILED,
    details = details,
)
