package com.waffletoy.team1server.post

import com.waffletoy.team1server.exceptions.ApiException
import com.waffletoy.team1server.exceptions.ErrorCode

class PostNotFoundException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.POST_NOT_FOUND,
    details = details,
)

class PostAlreadyBookmarkedException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.POST_ALREADY_BOOKMARKED,
    details = details,
)

class PostBookmarkNotFoundException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.POST_BOOKMARK_NOT_FOUND,
    details = details,
)

class PostInvalidFiltersException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.POST_INVALID_FILTERS,
    details = details,
)

class PostCreationFailedException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.POST_CREATION_FAILED,
    details = details,
)

class PostDeletionFailedException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.POST_DELETION_FAILED,
    details = details,
)
