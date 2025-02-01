package com.waffletoy.team1server.exceptions

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val code: String,
    val description: String,
    val httpStatus: HttpStatus,
) {
    // User-related errors
    USER_DUPLICATE_SNUMAIL("USER_001", "A user with the provided SNU mail already exists.", HttpStatus.CONFLICT),
    USER_DUPLICATE_LOCAL_ID("USER_002", "A user with the provided local login ID already exists.", HttpStatus.CONFLICT),
    USER_DUPLICATE_GOOGLE_ID("USER_003", "A user with the provided google login ID already exists.", HttpStatus.CONFLICT),
    USER_NOT_FOUND("USER_004", "User not found.", HttpStatus.NOT_FOUND),
    USER_INVALID_ROLE("USER_005", "Invalid user role.", HttpStatus.FORBIDDEN),
    USER_MERGE_UNKNOWN_FAILURE("USER_006", "User merge unknown failure.", HttpStatus.CONFLICT),
    USER_INVALID_METHOD("USER_007", "Invalid user method.", HttpStatus.FORBIDDEN),

    // Authentication-related errors
    AUTH_BAD_HEADER("AUTH_001", "Wrong Authorization header", HttpStatus.BAD_REQUEST),
    AUTH_INVALID_CREDENTIALS("AUTH_002", "Invalid credentials provided.", HttpStatus.UNAUTHORIZED),
    AUTH_INVALID_REFRESH_TOKEN("AUTH_003", "Invalid or expired refresh token.", HttpStatus.UNAUTHORIZED),
    AUTH_INVALID_ACCESS_TOKEN("AUTH_004", "Invalid access token.", HttpStatus.UNAUTHORIZED),
    AUTH_TOKEN_MISMATCH("AUTH_005", "Access token does not match the refresh token.", HttpStatus.BAD_REQUEST),
    AUTH_OAUTH_FAILURE("AUTH_006", "OAUTH failure.", HttpStatus.UNAUTHORIZED),
    AUTH_NOT_AUTHORIZED("AUTH_007", "Not authorized.", HttpStatus.UNAUTHORIZED),

    // Email-related errors
    EMAIL_VERIFICATION_FAILED("EMAIL_001", "Email verification failed.", HttpStatus.BAD_REQUEST),
    EMAIL_VERIFICATION_INVALID("EMAIL_002", "Invalid email verification code.", HttpStatus.BAD_REQUEST),
    EMAIL_VERIFICATION_SEND_FAILURE("EMAIL_003", "Failed to send verification email.", HttpStatus.INTERNAL_SERVER_ERROR),

    // General errors
    INTERNAL_SERVER_ERROR("GEN_001", "An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_IMPLEMENTED("GEN_002", "This functionality is not yet implemented.", HttpStatus.NOT_IMPLEMENTED),
    INVALID_REQUEST("GEN_003", "Invalid request parameters.", HttpStatus.BAD_REQUEST),
    VALIDATION_FAILED("GEN_004", "Validation failed for the request.", HttpStatus.BAD_REQUEST),
    INVALID_JSON("GEN_005", "Malformed JSON request.", HttpStatus.BAD_REQUEST),

    // Post-related errors
    POST_NOT_FOUND("POST_001", "The requested post was not found.", HttpStatus.NOT_FOUND),
    POST_ALREADY_BOOKMARKED("POST_002", "The post is already bookmarked.", HttpStatus.CONFLICT),
    POST_BOOKMARK_NOT_FOUND("POST_003", "The bookmark for the post does not exist.", HttpStatus.NOT_FOUND),
    POST_INVALID_FILTERS("POST_004", "Invalid filters provided for fetching posts.", HttpStatus.BAD_REQUEST),
    POST_CREATION_FAILED("POST_005", "Failed to create the post.", HttpStatus.INTERNAL_SERVER_ERROR),
    POST_DELETION_FAILED("POST_006", "Failed to delete the post.", HttpStatus.INTERNAL_SERVER_ERROR),
    POST_COMPANY_EXISTS("POST_007", "Company email already exists", HttpStatus.CONFLICT),
    POST_COMPANY_NOT_FOUND("POST_008", "Company not found.", HttpStatus.NOT_FOUND),
    POST_ACCESS_FORBIDDEN("POST_009", "Access to this company forbidden.", HttpStatus.FORBIDDEN),
    POST_S3_URL_GENERATION_FAILED("POST_010", "S3 URL generation failed.", HttpStatus.INTERNAL_SERVER_ERROR),
    POST_S3_SDK_FAILED("POST_011", "AWS SKD Client error", HttpStatus.INTERNAL_SERVER_ERROR),

    // Resume-related errors
    RESUME_NOT_FOUND("RESUME_001", "The requested resume was not found.", HttpStatus.NOT_FOUND),
    RESUME_FORBIDDEN("RESUME_002", "You do not have permission to perform this action.", HttpStatus.FORBIDDEN),
    RESUME_CREATION_FAILED("RESUME_003", "Failed to create the resume.", HttpStatus.INTERNAL_SERVER_ERROR),
    RESUME_DELETION_FAILED("RESUME_004", "Failed to delete the resume.", HttpStatus.INTERNAL_SERVER_ERROR),
    RESUME_UPDATE_FAILED("RESUME_005", "Failed to update the resume.", HttpStatus.INTERNAL_SERVER_ERROR),
    RESUME_INVALID_USER_ROLE("RESUME_006", "Invalid user role for resume operations.", HttpStatus.FORBIDDEN),
}
