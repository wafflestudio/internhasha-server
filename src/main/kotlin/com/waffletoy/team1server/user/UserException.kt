package com.waffletoy.team1server.user

import com.waffletoy.team1server.DomainException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class UserException(
    errorCode: Int,
    httpStatusCode: HttpStatusCode,
    msg: String,
    cause: Throwable? = null,
) : DomainException(errorCode, httpStatusCode, msg, cause)

class SignUpUserEmailConflictException : UserException(
    errorCode = 0,
    httpStatusCode = HttpStatus.CONFLICT,
    msg = "Username conflict",
)

class SignUpBadUsernameException : UserException(
    errorCode = 0,
    httpStatusCode = HttpStatus.BAD_REQUEST,
    msg = "Bad username",
)

// 401
class SignUpBadPasswordException : UserException(
    errorCode = 0,
    httpStatusCode = HttpStatus.BAD_REQUEST,
    msg = "Bad password",
)

class SignInUserNotFoundException : UserException(
    errorCode = 0,
    httpStatusCode = HttpStatus.UNAUTHORIZED,
    msg = "User not found",
)

class SignInInvalidPasswordException : UserException(
    errorCode = 0,
    httpStatusCode = HttpStatus.UNAUTHORIZED,
    msg = "Invalid password",
)

class AuthenticateException : UserException(
    errorCode = 0,
    httpStatusCode = HttpStatus.UNAUTHORIZED,
    msg = "Authenticate failed",
)

class RefreshTokenExpiredException : UserException(
    errorCode = 0,
    httpStatusCode = HttpStatus.UNAUTHORIZED,
    msg = "Token expired",
)

class RefreshTokenInvalidException : UserException(
    errorCode = 0,
    httpStatusCode = HttpStatus.UNAUTHORIZED,
    msg = "Invalid token",
)
