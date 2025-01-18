package com.waffletoy.team1server

import com.waffletoy.team1server.user.AuthenticateException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalControllerExceptionHandler {
    @ExceptionHandler(DomainException::class)
    fun handle(exception: DomainException): ResponseEntity<Map<String, Any>> {
        return ResponseEntity
            .status(exception.httpErrorCode)
            .body(mapOf("error" to exception.msg, "errorCode" to exception.errorCode))
    }

    @ExceptionHandler(AuthenticateException::class)
    fun handleAuthenticateException(ex: AuthenticateException): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.message)
    }
}
