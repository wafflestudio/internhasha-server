package com.wafflestudio.internhasha.post.application.service

import com.wafflestudio.internhasha.post.application.port.BookmarkPersistencePort
import com.wafflestudio.internhasha.post.application.port.PositionPersistencePort
import com.wafflestudio.internhasha.post.domain.Bookmark
import com.wafflestudio.internhasha.post.domain.Post
import com.wafflestudio.internhasha.post.domain.PostAlreadyBookmarkedException
import com.wafflestudio.internhasha.post.domain.PostBookmarkNotFoundException
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookmarkService(
    private val bookmarkPersistencePort: BookmarkPersistencePort,
    private val positionPersistencePort: PositionPersistencePort,
    @Value("\${custom.page.size:12}")
    private val pageSize: Int = 12,
) {
    @Transactional
    fun addBookmark(
        applicantId: String,
        positionId: String,
    ) {
        val isExists = bookmarkPersistencePort.existsByApplicantIdAndPositionId(applicantId, positionId)
        when (isExists) {
            true -> throw PostAlreadyBookmarkedException(details = mapOf("userId" to applicantId, "postId" to positionId))
            false ->
                bookmarkPersistencePort.createBookmark(
                    Bookmark(
                        id = "",
                        applicantId = applicantId,
                        positionId = positionId,
                    ),
                )
        }
    }

    @Transactional
    fun deleteBookmark(
        applicantId: String,
        positionId: String,
    ) {
        val bookmark =
            bookmarkPersistencePort.findBookmarkByApplicantAndPositionId(
                applicantId = applicantId,
                positionId = positionId,
            ) ?: throw PostBookmarkNotFoundException(mapOf("userId" to applicantId, "postId" to positionId))
        bookmarkPersistencePort.deleteBookmark(bookmark)
    }

    fun getBookmarks(
        applicantId: String,
        page: Int,
    ): Page<Post> {
        val validPage = if (page < 0) 0 else page
        val pageable = PageRequest.of(validPage, pageSize)
        val positions =
            bookmarkPersistencePort.getPositionsPageBookmarkedByApplicationId(
                applicantId = applicantId,
                pageable = pageable,
            )
        val coffeeChatCountMap =
            positionPersistencePort.countCoffeeChatByPositionIds(positions.content.map { it.id })
                .associateBy { it.positionId }

        return positions.map { position ->
            Post.fromPosition(
                position = position,
                isBookmarked = true,
                isLoggedIn = true,
                coffeeChatCount = coffeeChatCountMap[position.id]?.coffeeChatCount ?: 0,
            )
        }
    }
}
