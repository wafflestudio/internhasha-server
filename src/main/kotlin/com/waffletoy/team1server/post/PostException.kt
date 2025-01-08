package com.waffletoy.team1server.post

import com.waffletoy.team1server.DomainException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class PostException(
    errorCode: Int,
    httpStatusCode: HttpStatusCode,
    msg: String,
    cause: Throwable? = null,
) : DomainException(errorCode, httpStatusCode, msg, cause)

class PostServiceException(
    customMessage: String? = null,
    httpStatusCode: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
    errorCode: Int = 0,
) : PostException(
    errorCode = errorCode,
    httpStatusCode = httpStatusCode,
    msg = customMessage ?: "Post Service Failed",
)