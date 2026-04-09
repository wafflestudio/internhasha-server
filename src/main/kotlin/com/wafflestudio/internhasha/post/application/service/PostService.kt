package com.wafflestudio.internhasha.post.application.service

import com.wafflestudio.internhasha.auth.domain.User
import com.wafflestudio.internhasha.auth.domain.UserRole
import com.wafflestudio.internhasha.auth.persistence.*
import com.wafflestudio.internhasha.coffeeChat.application.port.CoffeeChatPersistencePort
import com.wafflestudio.internhasha.company.persistence.CompanyRepository
import com.wafflestudio.internhasha.exceptions.*
import com.wafflestudio.internhasha.post.*
import com.wafflestudio.internhasha.post.application.dto.CreatePositionCommand
import com.wafflestudio.internhasha.post.application.dto.UpdatePositionCommand
import com.wafflestudio.internhasha.post.application.port.BookmarkPersistencePort
import com.wafflestudio.internhasha.post.application.port.PositionPersistencePort
import com.wafflestudio.internhasha.post.domain.Bookmark
import com.wafflestudio.internhasha.post.domain.Position
import com.wafflestudio.internhasha.post.domain.Post
import com.wafflestudio.internhasha.post.domain.PostNotFoundException
import com.wafflestudio.internhasha.post.persistence.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class PostService(
    private val companyRepository: CompanyRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val positionRepository: PositionRepository,
    private val userPersistenceAdapter: UserPersistenceAdapter,
    private val coffeeChatPersistencePort: CoffeeChatPersistencePort,
    private val positionPersistencePort: PositionPersistencePort,
    private val bookmarkPersistencePort: BookmarkPersistencePort,
    @Value("\${custom.page.size:12}")
    private val pageSize: Int = 12,
) {
    fun getPageDetail(
        user: User?,
        postId: String,
    ): Post {
        val position =
            positionPersistencePort.findPositionById(postId)
                ?: throw PostNotFoundException(mapOf("postId" to postId))
        val bookmarkIds = getAllBookmark(user).map { it.id }.toSet()
        val coffeeChatCount =
            positionPersistencePort.countCoffeeChatByPositionIds(listOf(position.id))
                .firstOrNull()
                ?.coffeeChatCount ?: 0
        return Post.fromPosition(
            position = position,
            isBookmarked = position.id in bookmarkIds,
            isLoggedIn = user != null,
            coffeeChatCount = coffeeChatCount,
        )
    }

    fun getPosts(
        user: User?,
        cond: PositionSearchCond,
        page: Int = 0,
    ): Page<Post> {
        return searchPosts(user, cond, page, fetchBookmarks = true)
    }

    fun getPostByCompany(
        user: User,
        cond: PositionSearchCond,
        page: Int = 0,
    ): Page<Post> {
        return searchPosts(user, cond, page, fetchBookmarks = false)
    }

    private fun searchPosts(
        user: User?,
        cond: PositionSearchCond,
        page: Int,
        fetchBookmarks: Boolean,
    ): Page<Post> {
        val validPage = if (page < 0) 0 else page
        val pageable = PageRequest.of(validPage, pageSize)
        val positions = positionPersistencePort.searchPosition(cond, pageable)
        val positionIds = positions.content.map { it.id }

        val bookmarkIds =
            if (user != null && fetchBookmarks) {
                getAllBookmark(user).map { it.id }.toSet()
            } else {
                emptySet()
            }

        val coffeeChatCountMap =
            positionPersistencePort.countCoffeeChatByPositionIds(positionIds)
                .associateBy { it.positionId }

        return positions.map { position ->
            Post.fromPosition(
                position = position,
                isBookmarked = position.id in bookmarkIds,
                isLoggedIn = user != null,
                coffeeChatCount = coffeeChatCountMap[position.id]?.coffeeChatCount ?: 0,
            )
        }
    }

    @Transactional
    fun createPosition(
        command: CreatePositionCommand,
    ): Position {
        return positionPersistencePort.createPosition(command)
    }

    @Transactional
    fun updatePosition(
        positionId: String,
        command: UpdatePositionCommand,
    ): Position {
        val position =
            positionPersistencePort.findPositionById(positionId)
                ?: throw PostNotFoundException(mapOf("positionId" to positionId))
        if (command.companyId != position.company.id) {
            throw NotAuthorizedException()
        }
        return positionPersistencePort.updatePosition(positionId, command)
    }

    @Transactional
    @Deprecated("삭제 대신 마감 기능")
    fun deletePosition(
        user: User,
        positionId: String,
    ) {
//        val positionEntity = positionRepository.findByIdOrNull(positionId) ?: throw PostPositionNotFoundException(mapOf("positionId" to positionId))
//        if (user.userRole != UserRole.ROLE_COMPANY || positionEntity.company.user.id != user.id) {
//            throw NotAuthorizedException()
//        }
//        positionRepository.delete(positionEntity)
    }

    // 공고 즉시 마감
    @Transactional
    fun closePosition(
        company: User,
        positionId: String,
    ) {
        val position =
            positionPersistencePort.findPositionById(positionId)
                ?: throw PostNotFoundException(mapOf("positionId" to positionId))

        if (company.id != position.company.id) {
            throw NotAuthorizedException()
        }

        val newPosition =
            position.copy(
                employmentEndDate = LocalDateTime.now(),
            )
        positionPersistencePort.updatePosition(
            positionId = positionId,
            command =
                UpdatePositionCommand(
                    positionTitle = newPosition.positionTitle,
                    positionType = newPosition.positionType,
                    detail = newPosition.detail,
                    headCount = newPosition.headCount,
                    salary = newPosition.salary,
                    employmentEndDate = newPosition.employmentEndDate,
                    companyId = newPosition.company.id,
                ),
        )
    }

    private fun getAllBookmark(user: User?): List<Bookmark> {
        if (user == null || user.userRole != UserRole.ROLE_APPLICANT) return emptyList()
        return bookmarkPersistencePort.getAllBookmarkByApplicantId(user.id)
    }
}
