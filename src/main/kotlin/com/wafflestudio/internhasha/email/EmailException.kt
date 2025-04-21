package com.wafflestudio.internhasha.email

import com.wafflestudio.internhasha.exceptions.ApiException
import com.wafflestudio.internhasha.exceptions.ErrorCode

class EmailSendFailureException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.EMAIL_SEND_FAILURE,
    details = details,
)

class EmailDefaultException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.INTERNAL_SERVER_ERROR,
    details = details,
)
