package com.wafflestudio.internhasha.post.application.port

import com.wafflestudio.internhasha.post.domain.Bookmark
import com.wafflestudio.internhasha.post.domain.Position
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface BookmarkPersistencePort {
    fun findBookmarkByApplicantAndPositionId(
        applicantId: String,
        positionId: String,
    ): Bookmark?

    fun existsByApplicantIdAndPositionId(
        applicantId: String,
        positionId: String,
    ): Boolean

    fun getAllBookmarkByApplicantId(applicantId: String): List<Bookmark>

    fun getPositionsPageBookmarkedByApplicationId(
        applicantId: String,
        pageable: Pageable,
    ): Page<Position>

    fun createBookmark(bookmark: Bookmark): Bookmark

    fun deleteBookmark(bookmark: Bookmark)
}
