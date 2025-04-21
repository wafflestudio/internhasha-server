package com.waffletoy.internhasha.company

import com.waffletoy.internhasha.exceptions.ApiException
import com.waffletoy.internhasha.exceptions.ErrorCode

class CompanyNotFoundException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.COMPANY_NOT_FOUND,
    details = details,
)

class CompanyCreationFailedException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.COMPANY_CREATION_FAILED,
    details = details,
)
