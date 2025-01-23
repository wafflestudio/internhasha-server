// File: com/waffletoy/team1server/resume/exceptions/ResumeExceptions.kt
package com.waffletoy.team1server.resume

import com.waffletoy.team1server.exceptions.ApiException
import com.waffletoy.team1server.exceptions.ErrorCode

/**
 * Thrown when a resume with the specified ID does not exist.
 *
 * @param details Additional context about the error, such as the [resumeId].
 */
class ResumeNotFoundException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.RESUME_NOT_FOUND,
    details = details,
)

/**
 * Thrown when a user does not have permission to perform a resume-related action.
 *
 * @param details Additional context about the error, such as [userId] and [resumeId].
 */
class ResumeForbiddenException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.RESUME_FORBIDDEN,
    details = details,
)

/**
 * Thrown when there is an issue creating a resume.
 *
 * @param details Additional context about the error, such as the [userId] and [postId].
 */
class ResumeCreationFailedException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.RESUME_CREATION_FAILED,
    details = details,
)

/**
 * Thrown when there is an issue deleting a resume.
 *
 * @param details Additional context about the error, such as the [resumeId].
 */
class ResumeDeletionFailedException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.RESUME_DELETION_FAILED,
    details = details,
)

/**
 * Thrown when there is an issue updating a resume.
 *
 * @param details Additional context about the error, such as the [resumeId].
 */
class ResumeUpdateFailedException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.RESUME_UPDATE_FAILED,
    details = details,
)

/**
 * Thrown when a user with an invalid role attempts to perform resume-related actions.
 *
 * @param details Additional context about the error, such as the [userId] and [userRole].
 */
class ResumeInvalidUserRoleException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.RESUME_INVALID_USER_ROLE,
    details = details,
)

/**
 * Thrown when sending an email fails.
 *
 * @param details Additional context about the error, such as the recipient and error message.
 */
class EmailSendFailureException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.EMAIL_VERIFICATION_SEND_FAILURE,
    details = details,
)
