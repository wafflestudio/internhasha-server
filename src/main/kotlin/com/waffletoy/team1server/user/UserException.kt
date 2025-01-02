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

// class SignUpConflictException(
//    customMessage: String? = null,
// ) : UserException(
//        errorCode = 0,
//        httpStatusCode = HttpStatus.CONFLICT,
//        msg = customMessage ?: "Argument Conflict",
//    )
//
// // Null이 아니어야할 필드가 Null일 때
// class SignUpIllegalArgumentException(
//    customMessage: String? = null,
// ) : UserException(
//        errorCode = 0,
//        httpStatusCode = HttpStatus.BAD_REQUEST,
//        msg = customMessage ?: "Invalid Argument",
//    )
//
// // 조건에 맞지 않는 아이디, 비밀번호일 때
// class SignUpBadArgumentException(
//    customMessage: String? = null,
// ) : UserException(
//        errorCode = 0,
//        httpStatusCode = HttpStatus.BAD_REQUEST,
//        msg = customMessage ?: "Invalid Argument",
//    )
//
// // Null이 아니어야할 필드가 Null일 때
// class SignInIllegalArgumentException(
//    customMessage: String? = null,
// ) : UserException(
//        errorCode = 0,
//        httpStatusCode = HttpStatus.BAD_REQUEST,
//        msg = customMessage ?: "Invalid Argument",
//    )
//
// class SignInUserNotFoundException : UserException(
//    errorCode = 0,
//    httpStatusCode = HttpStatus.NOT_FOUND,
//    msg = "User not found",
// )
//
// class SignInInvalidPasswordException : UserException(
//    errorCode = 0,
//    httpStatusCode = HttpStatus.UNAUTHORIZED,
//    msg = "Invalid password",
// )
//
// class AuthenticateException : UserException(
//    errorCode = 0,
//    httpStatusCode = HttpStatus.UNAUTHORIZED,
//    msg = "Authenticate failed",
// )
//
// class RefreshTokenInvalidException(
//    customMessage: String? = null,
// ) : UserException(
//        errorCode = 0,
//        httpStatusCode = HttpStatus.BAD_REQUEST,
//        msg = customMessage ?: "Invalid Refresh token",
//    )
//
// class AccessTokenInvalidException(
//    customMessage: String? = null,
// ) : UserException(
//        errorCode = 0,
//        httpStatusCode = HttpStatus.BAD_REQUEST,
//        msg = customMessage ?: "Invalid Access token",
//    )
//
// class EmailTokenInvalidException(
//    customMessage: String? = null,
// ) : UserException(
//        errorCode = 0,
//        httpStatusCode = HttpStatus.BAD_REQUEST,
//        msg = customMessage ?: "Invalid Email token",
//    )
//
// class EmailSendException : UserException(
//    errorCode = 0,
//    httpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR,
//    msg = "Email send failed",
// )
//
// class UserNotFound(
//    customMessage: String? = null,
// ) : UserException(
//        errorCode = 0,
//        httpStatusCode = HttpStatus.NOT_FOUND,
//        msg = customMessage ?: "UserNotFound",
//    )

// //////////////////////////////////////////////////

class UserServiceException(
    customMessage: String? = null,
    httpStatusCode: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
    errorCode: Int = 0,
) : UserException(
        errorCode = errorCode,
        httpStatusCode = httpStatusCode,
        msg = customMessage ?: "User Login Service Failed",
    )

class GoogleOAuthServiceException(
    customMessage: String? = null,
    httpStatusCode: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
    errorCode: Int = 0,
) : UserException(
        errorCode = errorCode,
        httpStatusCode = httpStatusCode,
        msg = customMessage ?: "Google Authentication Failed",
    )

class EmailServiceException(
    customMessage: String? = null,
    httpStatusCode: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
    errorCode: Int = 0,
) : UserException(
        errorCode = errorCode,
        httpStatusCode = httpStatusCode,
        msg = customMessage ?: "Email Service Failed",
    )
