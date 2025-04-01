package com.waffletoy.team1server.company

import com.waffletoy.team1server.exceptions.ApiException
import com.waffletoy.team1server.exceptions.ErrorCode

class CompanyNotFoundException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.COMPANY_NOT_FOUND,
    details = details,
)

class CompanyCreationFailedException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.COMPANY_CREATION_FAILED,
    details = details,
)
