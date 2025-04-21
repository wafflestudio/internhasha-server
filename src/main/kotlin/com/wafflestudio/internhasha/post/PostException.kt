package com.wafflestudio.internhasha.post

import com.wafflestudio.internhasha.exceptions.ApiException
import com.wafflestudio.internhasha.exceptions.ErrorCode

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

class PostCompanyExistsException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.POST_COMPANY_EXISTS,
    details = details,
)

class PostCompanyNotFoundException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.POST_COMPANY_NOT_FOUND,
    details = details,
)

class PostPositionNotFoundException(details: Map<String, Any>? = null) : ApiException(
    errorCode = ErrorCode.POST_POSITION_NOT_FOUND,
    details = details,
)
