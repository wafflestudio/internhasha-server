package com.wafflestudio.internhasha.post.service

import com.wafflestudio.internhasha.applicant.repository.ApplicantRepository
import com.wafflestudio.internhasha.common.event.UserDeletedEvent
import com.wafflestudio.internhasha.post.domain.Bookmark
import com.wafflestudio.internhasha.post.exception.PostAlreadyBookmarkedException
import com.wafflestudio.internhasha.post.exception.PostBookmarkNotFoundException
import com.wafflestudio.internhasha.post.repository.BookmarkRepository
import com.wafflestudio.internhasha.post.repository.PositionRepository
import com.wafflestudio.internhasha.post.service.dto.PostDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.event.EventListener
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookmarkService(
    private val applicantRepository: ApplicantRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val positionRepository: PositionRepository,
    @param:Value("\${custom.page.size:12}")
    private val pageSize: Int = 12,
) {
    @Transactional
    fun addBookmark(
        applicantId: String,
        positionId: String,
    ) {
        val isExists = bookmarkRepository.existsByApplicantIdAndPositionId(applicantId, positionId)
        when (isExists) {
            true -> throw PostAlreadyBookmarkedException(details = mapOf("userId" to applicantId, "postId" to positionId))
            false ->
                bookmarkRepository.save(
                    Bookmark(
                        applicant = applicantRepository.getReferenceById(applicantId),
                        position = positionRepository.getReferenceById(positionId),
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
            bookmarkRepository.findByApplicantIdAndPositionId(
                applicantId = applicantId,
                positionId = positionId,
            ) ?: throw PostBookmarkNotFoundException(mapOf("userId" to applicantId, "postId" to positionId))
        bookmarkRepository.deleteById(bookmark.requireId())
    }

    fun getBookmarks(
        applicantId: String,
        page: Int,
    ): Page<PostDto> {
        val validPage = if (page < 0) 0 else page
        val pageable = PageRequest.of(validPage, pageSize)
        val bookmarks =
            bookmarkRepository.findAllByApplicantIdWithFetchJoin(
                applicantId = applicantId,
                pageable = pageable,
            )
        val coffeeChatCountMap =
            positionRepository
                .countCoffeeChatByPositionIds(bookmarks.content.map { it.requireId() })
                .associateBy { it.positionId }

        return bookmarks.map { bookmark ->
            PostDto.fromEntity(
                entity = bookmark.position,
                isBookmarked = true,
                isLoggedIn = true,
                coffeeChatCount = coffeeChatCountMap[bookmark.id]?.coffeeChatCount ?: 0,
            )
        }
    }

    @EventListener
    fun handleUserDeletedEvent(event: UserDeletedEvent) {
        bookmarkRepository.deleteAllByApplicantId(event.userId)
    }
}
