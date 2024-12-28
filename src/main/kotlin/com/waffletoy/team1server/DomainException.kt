package com.waffletoy.team1server

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

open class DomainException(
    // client 와 약속된 Application Error 에 대한 코드 필요 시 Enum 으로 관리하자.
    val errorCode: Int,
    // HTTP Status Code, 비어있다면 500 이다.
    val httpErrorCode: HttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR,
    val msg: String,
    cause: Throwable? = null,
) : RuntimeException(msg, cause) {
    override fun toString(): String = "com.waffletoy.team1server.DomainException(msg='$msg', errorCode=$errorCode, httpErrorCode=$httpErrorCode)"
}
