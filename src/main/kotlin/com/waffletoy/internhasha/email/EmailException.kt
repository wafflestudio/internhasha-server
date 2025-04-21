package com.waffletoy.internhasha.email

import com.waffletoy.internhasha.exceptions.ApiException
import com.waffletoy.internhasha.exceptions.ErrorCode

class EmailSendFailureException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.EMAIL_SEND_FAILURE,
    details = details,
)

class EmailDefaultException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.INTERNAL_SERVER_ERROR,
    details = details,
)
