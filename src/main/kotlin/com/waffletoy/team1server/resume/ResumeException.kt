package com.waffletoy.team1server.resume

import com.waffletoy.team1server.DomainException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class ResumeException(
    errorCode: Int,
    httpStatusCode: HttpStatusCode,
    msg: String,
    cause: Throwable? = null,
) : DomainException(errorCode, httpStatusCode, msg, cause)

class ResumeServiceException(
    customMessage: String? = null,
    httpStatusCode: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
    errorCode: Int = 0,
) : ResumeException(
        errorCode = errorCode,
        httpStatusCode = httpStatusCode,
        msg = customMessage ?: "Post Service Failed",
    )
