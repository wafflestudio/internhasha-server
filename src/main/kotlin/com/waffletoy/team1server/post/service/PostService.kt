package com.waffletoy.team1server.post.service

import com.waffletoy.team1server.auth.UserNotFoundException
import com.waffletoy.team1server.auth.UserRole
import com.waffletoy.team1server.auth.dto.User
import com.waffletoy.team1server.auth.persistence.*
import com.waffletoy.team1server.auth.service.AuthService
import com.waffletoy.team1server.company.dto.TagVo
import com.waffletoy.team1server.company.persistence.CompanyEntity
import com.waffletoy.team1server.company.persistence.CompanyRepository
import com.waffletoy.team1server.exceptions.*
import com.waffletoy.team1server.post.*
import com.waffletoy.team1server.post.Category
import com.waffletoy.team1server.post.dto.*
import com.waffletoy.team1server.post.persistence.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PostService(
    private val companyRepository: CompanyRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val positionRepository: PositionRepository,
    @Lazy private val authService: AuthService,
) {
    @Value("\${custom.page.size:12}")
    private val pageSize: Int = 12

    /**
     * Retrieves detailed information of a specific post by its ID.
     *
     * @param user nullable user field to get bookmark
     * @param postId The unique identifier of the post.
     * @return The detailed [Post] object.
     * @throws PostNotFoundException If the post with the given ID does not exist.
     */
    @Transactional(readOnly = true)
    fun getPageDetail(
        user: User?,
        postId: String,
    ): Post {
        val positionEntity = getPositionEntityOrThrow(postId)
        val bookmarkIds = getBookmarkIds(user)
        return Post.fromEntity(
            entity = positionEntity,
            isBookmarked = positionEntity.id in bookmarkIds,
            isLoggedIn = user != null,
        )
    }

    /**
     * Retrieves a paginated list of posts based on provided filters.
     *
     * @param user nullable user field to get bookmark
     * @param positions List of position names to filter by.
     * @param order Sort posts by newest(0) or employmentEndDate(1)
     * @param page The page number to retrieve.
     * @return A paginated [Page] of [Post].
     */
    @Transactional(readOnly = true)
    fun getPosts(
        user: User?,
        positions: List<String>?,
        page: Int = 0,
        order: Int = 0,
    ): Page<Post> {
        val specification =
            PositionSpecification.withFilters(
                positions = positions,
                order = order,
            )

        val validPage = if (page < 0) 0 else page
        val pageable = PageRequest.of(validPage, pageSize)

        val positionPage = positionRepository.findAll(specification, pageable)

        val bookmarkIds = getBookmarkIds(user)
        val isLoggedIn = user != null

        return positionPage.map { position ->
            Post.fromEntity(
                entity = position,
                isBookmarked = position.id in bookmarkIds,
                isLoggedIn = isLoggedIn,
            )
        }
    }

    /**
     * Adds a bookmark for a user on a specific post.
     *
     * @param userId The unique identifier of the user.
     * @param postId The unique identifier of the post.
     * @throws PostNotFoundException If the post does not exist.
     * @throws UserNotFoundException If the user does not exist.
     * @throws PostAlreadyBookmarkedException If the post is already bookmarked by the user.
     */
    @Transactional
    fun addBookmark(
        userId: String,
        postId: String,
    ) {
        val positionEntity = getPositionEntityOrThrow(postId)

        val userEntity = getUserEntityOrThrow(userId)

        val existingBookmark = bookmarkRepository.findByUserAndPosition(userEntity, positionEntity)
        if (existingBookmark != null) {
            throw PostAlreadyBookmarkedException(
                details = mapOf("userId" to userId, "postId" to postId),
            )
        }

        bookmarkRepository.save(
            BookmarkEntity(
                position = positionEntity,
                user = userEntity,
            ),
        )
    }

    /**
     * Removes a bookmark for a user on a specific post.
     *
     * @param userId The unique identifier of the user.
     * @param postId The unique identifier of the post.
     * @throws PostNotFoundException If the post does not exist.
     * @throws UserNotFoundException If the user does not exist.
     * @throws PostBookmarkNotFoundException If the bookmark does not exist.
     */
    @Transactional
    fun deleteBookmark(
        userId: String,
        postId: String,
    ) {
        val positionEntity = getPositionEntityOrThrow(postId)

        val userEntity = getUserEntityOrThrow(userId)

        val bookmarkEntity =
            bookmarkRepository.findByUserAndPosition(userEntity, positionEntity) ?: throw PostBookmarkNotFoundException(
                details = mapOf("userId" to userId, "postId" to postId),
            )
        bookmarkRepository.delete(bookmarkEntity)
    }

    /**
     * Retrieves a paginated list of bookmarks for a specific user.
     *
     * @param userId The unique identifier of the user.
     * @param page The page number to retrieve.
     * @return A paginated [Page] of [Post] representing bookmarked posts.
     * @throws UserNotFoundException If the user does not exist.
     */
    @Transactional(readOnly = true)
    fun getBookmarks(
        userId: String,
        page: Int,
    ): Page<Post> {
        val userEntity = getUserEntityOrThrow(userId)

        val validPage = if (page < 0) 0 else page
        val pageable = PageRequest.of(validPage, pageSize)
        val positionPage = bookmarkRepository.findPositionsByUser(userEntity, pageable)
        return positionPage.map { position ->
            Post.fromEntity(
                entity = position,
                isBookmarked = true,
                isLoggedIn = true,
            )
        }
    }

    /**
     * Creates dummy posts for testing or development purposes.
     *
     * @param cnt The number of dummy posts to create.
     */
    @Transactional
    fun makeDummyPosts(
        cnt: Int,
        secret: String,
    ) {
        if (secret != devSecret) {
            throw InvalidRequestException(
                details = mapOf("providedSecret" to secret),
            )
        }

        val companies = mutableListOf<CompanyEntity>()
        val positions = mutableListOf<PositionEntity>()

        (1..cnt).forEach { index ->
            val user = authService.makeDummyUser(index)
            val tags =
                listOf("Tech", "Finance", "Health")
                    .shuffled()
                    .take(2)
                    .map { TagVo(it) }
                    .toMutableList()

            val companyEntity =
                CompanyEntity(
                    user = user,
                    detail = "Explanation of dummy Company $index",
                    companyEstablishedYear = 2000 + index,
                    slogan = "Slogan of dummy$index",
                    domain = "www.company$index.com",
                    headcount = (10..500).random(),
                    location = "Location $index",
                    profileImageKey = "profile-image-key-$index",
                    companyInfoPDFLink = "https://www.company$index/info.pdf",
                    landingPageLink = "https://www.company$index",
                    vcName = "Mr. Hoon",
                    vcRec = "아주 좋아요",
                    tags = tags,
                )

            companies.add(companyEntity)

            Category.entries.shuffled().take((1..3).random()).forEach { category ->
                positions.add(
                    PositionEntity(
                        positionTitle = "Title of $index",
                        positionType = category,
                        detail = "Detail of $category",
                        headCount = (1..3).random(),
                        isActive = true,
                        employmentEndDate = LocalDateTime.now().plusHours((-15..15).random().toLong()),
                        company = companyEntity,
                        salary = 10,
                    ),
                )
            }
        }

        companyRepository.saveAll(companies)
        positionRepository.saveAll(positions)
    }

//    /**
//     * Resets the database by deleting all companies, bookmarks, and positions.
//     */
//    @Transactional
//    fun resetDB(secret: String) {
//        if (secret != resetDbSecret) {
//            throw InvalidRequestException(
//                details = mapOf("providedSecret" to secret),
//            )
//        }
//        companyRepository.deleteAll()
//        bookmarkRepository.deleteAll()
//        positionRepository.deleteAll()
//    }

    fun getUserEntityOrThrow(userId: String): UserEntity =
        authService.getUserEntityByUserId(userId) ?: throw UserNotFoundException(mapOf("userId" to userId))

    fun getPositionEntityOrThrow(postId: String): PositionEntity =
        positionRepository.findByIdOrNull(postId) ?: throw PostNotFoundException(mapOf("postId" to postId))

    fun getPositionEntityByPostId(postId: String): PositionEntity? = positionRepository.findByIdOrNull(postId)

    fun getBookmarkIds(user: User?): Set<String> {
        if (user == null) {
            return emptySet()
        }

        val userEntity = authService.getUserEntityByUserId(user.id)
        return if (userEntity != null) {
            bookmarkRepository.findByUser(userEntity).map { it.position.id }.toSet()
        } else {
            emptySet()
        }
    }
    /*
    @Transactional
    fun deleteCompany(
        user: User,
        companyId: String,
    ) {
        val companyEntity = companyRepository.findByIdOrNull(companyId) ?: throw PostCompanyNotFoundException(mapOf("companyId" to companyId))
        if (user.userRole != UserRole.COMPANY || companyEntity.user.id != user.id) {
            throw NotAuthorizedException()
        }
        companyRepository.delete(companyEntity)
    }
     */

    @Transactional
    fun getPostByCompany(
        user: User,
        positions: List<String>?,
        page: Int = 0,
        order: Int = 0,
    ): Page<Post> {
        if (user.userRole != UserRole.COMPANY) {
            throw NotAuthorizedException()
        }

        val userEntity =
            authService.getUserEntityByUserId(user.id)
                ?: throw UserNotFoundException(mapOf("userId" to user.id))

        val specification =
            PositionSpecification.withFilters(
                positions = positions,
                order = order,
                company = userEntity,
            )

        val validPage = if (page < 0) 0 else page
        val pageable = PageRequest.of(validPage, pageSize)
        val positionPage = positionRepository.findAll(specification, pageable)

        val bookmarkIds = getBookmarkIds(user)

        return positionPage.map { position ->
            Post.fromEntity(position, isBookmarked = position.id in bookmarkIds, isLoggedIn = true)
        }
    }

    @Transactional
    fun createPosition(
        user: User,
        request: CreatePositionRequest,
    ): Position {
        if (user.userRole != UserRole.COMPANY) {
            throw NotAuthorizedException()
        }
        // Retrieve the company by ID
        val company =
            companyRepository.findByIdOrNull(request.companyId)
                ?: throw PostCompanyNotFoundException(mapOf("companyId" to (request.companyId ?: "null")))

        // Check if the user is the owner of the company
        if (company.user.id != user.id) {
            throw NotAuthorizedException()
        }

        // Map CreatePositionRequest to PositionEntity
        val positionEntity =
            PositionEntity(
                positionTitle = request.positionTitle,
                positionType = request.positionType,
                detail = request.detail,
                headCount = request.headCount,
                salary = request.salary,
                employmentEndDate = request.employmentEndDate,
                isActive = request.isActive,
                company = company,
            )

        // Save the PositionEntity
        val savedPosition = positionRepository.save(positionEntity)

        // Convert to Position DTO
        return Position.fromEntity(savedPosition)
    }

    @Transactional
    fun updatePosition(
        user: User,
        positionId: String,
        request: UpdatePositionRequest,
    ): Position {
        var positionEntity = positionRepository.findByIdOrNull(positionId) ?: throw PostPositionNotFoundException(mapOf("positionId" to positionId))
        if (user.userRole != UserRole.COMPANY || positionEntity.company.user.id != user.id) {
            throw NotAuthorizedException()
        }
        positionEntity = updatePositionEntityWithRequest(positionEntity, request)
        return Position.fromEntity(positionEntity)
    }

    private fun updatePositionEntityWithRequest(
        positionEntity: PositionEntity,
        request: UpdatePositionRequest,
    ): PositionEntity {
        positionEntity.positionTitle = request.positionTitle
        positionEntity.positionType = request.positionType
        positionEntity.detail = request.detail
        positionEntity.headCount = request.headCount
        positionEntity.salary = request.salary
        positionEntity.employmentEndDate = request.employmentEndDate
        positionEntity.isActive = request.isActive
        return positionEntity
    }

    @Transactional
    fun deletePosition(
        user: User,
        positionId: String,
    ) {
        val positionEntity = positionRepository.findByIdOrNull(positionId) ?: throw PostPositionNotFoundException(mapOf("positionId" to positionId))
        if (user.userRole != UserRole.COMPANY || positionEntity.company.user.id != user.id) {
            throw NotAuthorizedException()
        }
        positionRepository.delete(positionEntity)
    }

    // 지원자 탈퇴 시 bookmark 데이터를 삭제
    @Transactional(propagation = Propagation.REQUIRED)
    fun deleteBookmarkByUser(userEntity: UserEntity) {
        bookmarkRepository.deleteAllByUser(userEntity)
    }

    @Value("\${custom.SECRET}")
    private lateinit var devSecret: String
}
