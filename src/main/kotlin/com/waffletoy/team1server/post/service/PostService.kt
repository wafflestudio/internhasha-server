package com.waffletoy.team1server.post.service

import com.waffletoy.team1server.exceptions.*
import com.waffletoy.team1server.post.*
import com.waffletoy.team1server.post.Category
import com.waffletoy.team1server.post.Series
import com.waffletoy.team1server.post.dto.Post
import com.waffletoy.team1server.post.dto.TagVo
import com.waffletoy.team1server.post.persistence.*
import com.waffletoy.team1server.user.dtos.User
import com.waffletoy.team1server.user.persistence.*
import com.waffletoy.team1server.user.service.UserService
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PostService(
    private val companyRepository: CompanyRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val positionRepository: PositionRepository,
    private val userService: UserService,
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
        )
    }

    /**
     * Retrieves a paginated list of posts based on provided filters.
     *
     * @param user nullable user field to get bookmark
     * @param positions List of position names to filter by.
     * @param investmentMax Maximum investment amount.
     * @param investmentMin Minimum investment amount.
     * @param status Status filter (e.g., active, inactive).
     * @param series List of series names to filter by.
     * @param page The page number to retrieve.
     * @return A paginated [Page] of [Post].
     * @throws PostInvalidFiltersException If invalid filters are provided.
     */
    @Transactional(readOnly = true)
    fun getPosts(
        user: User?,
        positions: List<String>?,
        investmentMax: Int?,
        investmentMin: Int?,
        status: Int?,
        series: List<String>?,
        page: Int = 0,
    ): Page<Post> {
        // Example validation: investmentMin should not exceed investmentMax
        if (investmentMin != null && investmentMax != null && investmentMin > investmentMax) {
            throw PostInvalidFiltersException(
                details =
                    mapOf(
                        "investmentMin" to investmentMin,
                        "investmentMax" to investmentMax,
                    ),
            )
        }

        val specification =
            PositionSpecification.withFilters(
                positions,
                investmentMax,
                investmentMin,
                status ?: 2,
                series,
            )

        val validPage = if (page < 0) 0 else page
        val pageable = PageRequest.of(validPage, pageSize)
        val positionPage = positionRepository.findAll(specification, pageable)

        val bookmarkIds = getBookmarkIds(user)

        return positionPage.map { position ->
            Post.fromEntity(
                entity = position,
                isBookmarked = position.id in bookmarkIds,
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
            )
        }
    }

    /**
     * Creates dummy posts for testing or development purposes.
     *
     * @param cnt The number of dummy posts to create.
     */
    @Transactional
    fun makeDummyPosts(cnt: Int) {
        val companies = mutableListOf<CompanyEntity>()
        val positions = mutableListOf<PositionEntity>()

        (1..cnt).forEach { index ->
            val admin = userService.makeDummyUser(index)
            val tags =
                listOf("Tech", "Finance", "Health")
                    .shuffled()
                    .take(2)
                    .map { TagVo(it) }
                    .toMutableList()

            val companyEntity =
                CompanyEntity(
                    admin = admin,
                    companyName = "dummy Company $index",
                    explanation = "Explanation of dummy Company $index",
                    email = "dummy$index@example.com",
                    slogan = "Slogan of dummy$index",
                    investAmount = (1000..5000).random(),
                    investCompany = "Company A$index, Company B$index",
                    series = Series.entries.random(),
                    imageLink = "https://www.company$index/image",
                    tags = tags,
                )
            companies.add(companyEntity)

            Category.entries.shuffled().take((1..3).random()).forEach { category ->
                positions.add(
                    PositionEntity(
                        title = "Title of $index",
                        category = category,
                        detail = "Detail of $category",
                        headcount = "${(1..3).random()}",
                        isActive = true,
                        employmentEndDate = LocalDateTime.now().plusHours((-15..15).random().toLong()),
                        company = companyEntity,
                    ),
                )
            }
        }

        companyRepository.saveAll(companies)
        positionRepository.saveAll(positions)
    }

    /**
     * Resets the database by deleting all companies, bookmarks, and positions.
     */
    @Transactional
    fun resetDB(secret: String) {
        if (secret != resetDbSecret) {
            throw InvalidRequestException(
                details = mapOf("providedSecret" to secret),
            )
        }
        companyRepository.deleteAll()
        bookmarkRepository.deleteAll()
        positionRepository.deleteAll()
    }

    fun getUserEntityOrThrow(userId: String): UserEntity =
        userService.getUserEntityByUserId(userId) ?: throw UserNotFoundException(mapOf("userId" to userId))

    fun getPositionEntityOrThrow(postId: String): PositionEntity =
        positionRepository.findByIdOrNull(postId) ?: throw PostNotFoundException(mapOf("postId" to postId))

    fun getPositionEntityByPostId(postId: String): PositionEntity? = positionRepository.findByIdOrNull(postId)

    fun getBookmarkIds(user: User?): Set<String> {
        if (user == null) {
            return emptySet()
        }

        val userEntity = userService.getUserEntityByUserId(user.id)
        return if (userEntity != null) {
            bookmarkRepository.findByUser(userEntity).map { it.position.id }.toSet()
        } else {
            emptySet()
        }
    }

    @Value("\${custom.SECRET}")
    private lateinit var resetDbSecret: String
}
