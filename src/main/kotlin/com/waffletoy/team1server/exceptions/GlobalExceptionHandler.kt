package com.waffletoy.team1server.exceptions

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(ApiException::class)
    fun handleApiException(ex: ApiException): ResponseEntity<ErrorResponse> {
        logger.error("API Exception: ${ex.errorCode.code} - ${ex.message}", ex)
        val errorResponse =
            ErrorResponse(
                code = ex.errorCode.code,
                message = ex.message,
                details = ex.details,
            )
        return ResponseEntity.status(ex.errorCode.httpStatus).body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        logger.error("Validation Exception: ${ex.message}", ex)

        // Extract field errors and construct details map
        val fieldErrors = ex.bindingResult.fieldErrors
        val details =
            fieldErrors.associate {
                it.field to (it.defaultMessage ?: "Invalid value")
            }

        val errorResponse =
            ErrorResponse(
                code = ErrorCode.VALIDATION_FAILED.code,
                message = ErrorCode.VALIDATION_FAILED.description,
                details = details,
            )
        return ResponseEntity.status(ErrorCode.VALIDATION_FAILED.httpStatus).body(errorResponse)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        logger.error("JSON Parsing Exception: ${ex.message}", ex)
        val errorResponse =
            ErrorResponse(
                code = ErrorCode.INVALID_JSON.code,
                message = ErrorCode.INVALID_JSON.description,
                details = mapOf("error" to ex.localizedMessage),
            )
        return ResponseEntity.status(ErrorCode.INVALID_JSON.httpStatus).body(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected Exception: ${ex.message}", ex)
        val errorResponse =
            ErrorResponse(
                code = ErrorCode.INTERNAL_SERVER_ERROR.code,
                message = ErrorCode.INTERNAL_SERVER_ERROR.description,
                details = null,
            )
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.httpStatus).body(errorResponse)
    }
}
