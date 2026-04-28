package com.wafflestudio.internhasha.post.service

import com.wafflestudio.internhasha.auth.domain.UserRole
import com.wafflestudio.internhasha.auth.security.AuthUserPrincipal
import com.wafflestudio.internhasha.company.repository.CompanyRepository
import com.wafflestudio.internhasha.exceptions.*
import com.wafflestudio.internhasha.post.domain.Position
import com.wafflestudio.internhasha.post.exception.PostNotFoundException
import com.wafflestudio.internhasha.post.repository.*
import com.wafflestudio.internhasha.post.service.dto.CreatePositionCommand
import com.wafflestudio.internhasha.post.service.dto.PositionDto
import com.wafflestudio.internhasha.post.service.dto.PositionSearchCond
import com.wafflestudio.internhasha.post.service.dto.PostDto
import com.wafflestudio.internhasha.post.service.dto.UpdatePositionCommand
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class PostService(
    private val companyRepository: CompanyRepository,
    private val positionRepository: PositionRepository,
    private val bookmarkRepository: BookmarkRepository,
    @Value("\${custom.page.size:12}")
    private val pageSize: Int = 12,
) {
    fun getPageDetail(
        authUser: AuthUserPrincipal?,
        postId: String,
    ): PostDto {
        val position =
            positionRepository.findByIdOrNull(postId)
                ?: throw PostNotFoundException(mapOf("postId" to postId))
        val bookmarkIds = getAllBookmarkIds(authUser)
        val coffeeChatCount =
            positionRepository.countCoffeeChatByPositionIds(listOf(position.requireId()))
                .firstOrNull()
                ?.coffeeChatCount ?: 0
        return PostDto.fromEntity(
            entity = position,
            isBookmarked = position.id in bookmarkIds,
            isLoggedIn = (authUser != null),
            coffeeChatCount = coffeeChatCount,
        )
    }

    fun getPosts(
        authUser: AuthUserPrincipal?,
        cond: PositionSearchCond,
        page: Int = 0,
    ): Page<PostDto> {
        return searchPosts(authUser, cond, page, fetchBookmarks = true)
    }

    fun getPostByCompany(
        authUser: AuthUserPrincipal,
        cond: PositionSearchCond,
        page: Int = 0,
    ): Page<PostDto> {
        return searchPosts(authUser, cond, page, fetchBookmarks = false)
    }

    private fun searchPosts(
        authUser: AuthUserPrincipal?,
        cond: PositionSearchCond,
        page: Int,
        fetchBookmarks: Boolean,
    ): Page<PostDto> {
        val validPage = if (page < 0) 0 else page
        val pageable = PageRequest.of(validPage, pageSize)
        val positions = positionRepository.searchPosition(cond, pageable)
        val positionIds = positions.content.map { it.requireId() }

        val bookmarkIds =
            if (authUser != null && fetchBookmarks) {
                getAllBookmarkIds(authUser)
            } else {
                emptySet()
            }

        val coffeeChatCountMap =
            positionRepository.countCoffeeChatByPositionIds(positionIds)
                .associateBy { it.positionId }

        return positions.map { position ->
            PostDto.fromEntity(
                entity = position,
                isBookmarked = position.requireId() in bookmarkIds,
                isLoggedIn = authUser != null,
                coffeeChatCount = coffeeChatCountMap[position.id]?.coffeeChatCount ?: 0,
            )
        }
    }

    @Transactional
    fun createPosition(
        command: CreatePositionCommand,
    ): PositionDto {
        val position =
            positionRepository.save(
                Position(
                    positionTitle = command.positionTitle,
                    positionType = command.positionType,
                    detail = command.detail,
                    headCount = command.headCount,
                    employmentEndDate = command.employmentEndDate,
                    salary = command.salary,
                    company = companyRepository.getReferenceById(command.companyId),
                ),
            )
        return PositionDto.fromEntity(position)
    }

    @Transactional
    fun updatePosition(
        positionId: String,
        command: UpdatePositionCommand,
    ): PositionDto {
        val position =
            positionRepository.findByIdOrNull(positionId)
                ?: throw PostNotFoundException(mapOf("positionId" to positionId))
        if (command.companyId != position.company.id) {
            throw NotAuthorizedException()
        }
        position.apply {
            positionTitle = command.positionTitle
            positionType = command.positionType
            detail = command.detail
            headCount = command.headCount
            employmentEndDate = command.employmentEndDate
            salary = command.salary
        }
        return PositionDto.fromEntity(position)
    }

    @Transactional
    @Deprecated("삭제 대신 마감 기능")
    fun deletePosition(
        authUser: AuthUserPrincipal,
        positionId: String,
    ) {
//        val positionEntity = positionRepository.findByIdOrNull(positionId) ?: throw PostPositionNotFoundException(mapOf("positionId" to positionId))
//        if (authUser.userRole != UserRole.ROLE_COMPANY || positionEntity.company.user.id != authUser.id) {
//            throw NotAuthorizedException()
//        }
//        positionRepository.delete(positionEntity)
    }

    // 공고 즉시 마감
    @Transactional
    fun closePosition(
        company: AuthUserPrincipal,
        positionId: String,
    ) {
        val position =
            positionRepository.findByIdOrNull(positionId)
                ?: throw PostNotFoundException(mapOf("positionId" to positionId))

        if (company.id != position.company.id) {
            throw NotAuthorizedException()
        }

        position.employmentEndDate = LocalDateTime.now()
    }

    private fun getAllBookmarkIds(authUser: AuthUserPrincipal?): Set<String> {
        if (authUser == null || authUser.userRole != UserRole.ROLE_APPLICANT) return emptySet()
        return bookmarkRepository.findAllByApplicantId(authUser.id).map { it.requireId() }.toSet()
    }
}
