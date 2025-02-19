package com.waffletoy.team1server.coffeeChat

import com.waffletoy.team1server.exceptions.ApiException
import com.waffletoy.team1server.exceptions.ErrorCode

/**
 * Thrown when a coffeeChat with the specified ID does not exist.
 *
 * @param details Additional context about the error, such as the [coffeeChatId].
 */
class CoffeeChatNotFoundException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.COFFEECHAT_NOT_FOUND,
    details = details,
)

/**
 * Thrown when a user does not have permission to perform a coffeeChat-related action.
 *
 * @param details Additional context about the error, such as [userId] and [coffeeChatId].
 */
class CoffeeChatForbiddenException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.COFFEECHAT_FORBIDDEN,
    details = details,
)

/**
 * Thrown when there is an issue creating a coffeeChat.
 *
 * @param details Additional context about the error, such as the [userId] and [postId].
 */
class CoffeeChatCreationFailedException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.COFFEECHAT_CREATION_FAILED,
    details = details,
)

/**
 * Thrown when there is an issue deleting a coffeeChat.
 *
 * @param details Additional context about the error, such as the [coffeeChatId].
 */
class CoffeeChatDeletionFailedException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.COFFEECHAT_DELETION_FAILED,
    details = details,
)

/**
 * Thrown when there is an issue updating a coffeeChat.
 *
 * @param details Additional context about the error, such as the [coffeeChatId].
 */
class CoffeeChatUpdateFailedException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.COFFEECHAT_UPDATE_FAILED,
    details = details,
)

/**
 * Thrown when a user with an invalid role attempts to perform coffeeChat-related actions.
 *
 * @param details Additional context about the error, such as the [userId] and [userRole].
 */
class CoffeeChatInvalidUserRoleException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.COFFEECHAT_INVALID_USER_ROLE,
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
