package com.waffletoy.team1server.email

import com.waffletoy.team1server.exceptions.ApiException
import com.waffletoy.team1server.exceptions.ErrorCode

class EmailSendFailureException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.EMAIL_SEND_FAILURE,
    details = details,
)

class EmailDefaultException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.INTERNAL_SERVER_ERROR,
    details = details,
)
