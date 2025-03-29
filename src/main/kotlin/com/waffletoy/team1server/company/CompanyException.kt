package com.waffletoy.team1server.company

import com.waffletoy.team1server.exceptions.ApiException
import com.waffletoy.team1server.exceptions.ErrorCode

class CompanyNotFound(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.POST_NOT_FOUND,
    details = details,
)