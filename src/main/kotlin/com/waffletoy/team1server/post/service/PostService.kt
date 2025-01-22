package com.waffletoy.team1server.post.service

import com.waffletoy.team1server.exceptions.*
import com.waffletoy.team1server.post.*
import com.waffletoy.team1server.post.Category
import com.waffletoy.team1server.post.Series
import com.waffletoy.team1server.post.dto.Link
import com.waffletoy.team1server.post.dto.Post
import com.waffletoy.team1server.post.dto.Tag
import com.waffletoy.team1server.post.persistence.*
import com.waffletoy.team1server.user.UserRole
import com.waffletoy.team1server.user.persistence.*
import org.mindrot.jbcrypt.BCrypt
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
    private val roleRepository: RoleRepository,
    private val userRepository: UserRepository,
) {
    @Value("\${custom.page.size:12}")
    private val pageSize: Int = 12

    /**
     * Retrieves detailed information of a specific post by its ID.
     *
     * @param postId The unique identifier of the post.
     * @return The detailed [Post] object.
     * @throws PostNotFoundException If the post with the given ID does not exist.
     */
    fun getPageDetail(postId: String): Post {
        val postEntity =
            roleRepository.findByIdOrNull(postId) ?: throw PostNotFoundException(
                details = mapOf("postId" to postId),
            )
        return Post.fromEntity(postEntity)
    }

    /**
     * Retrieves a paginated list of posts based on provided filters.
     *
     * @param roles List of role names to filter by.
     * @param investmentMax Maximum investment amount.
     * @param investmentMin Minimum investment amount.
     * @param status Status filter (e.g., active, inactive).
     * @param series List of series names to filter by.
     * @param page The page number to retrieve.
     * @return A paginated [Page] of [RoleEntity].
     * @throws PostInvalidFiltersException If invalid filters are provided.
     */
    fun getPosts(
        roles: List<String>?,
        investmentMax: Int?,
        investmentMin: Int?,
        status: Int?,
        series: List<String>?,
        page: Int = 0,
    ): Page<RoleEntity> {
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
            RoleSpecification.withFilters(
                roles,
                investmentMax,
                investmentMin,
                status ?: 2,
                series,
            )

        val pageable = PageRequest.of(page, pageSize)

        val roleIds = roleRepository.findAll(specification).map { it.id }

        // Fetch paginated RoleEntities
        return roleRepository.findAllByIdIn(roleIds, pageable)
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
    fun bookmarkPost(
        userId: String,
        postId: String,
    ) {
        val roleEntity =
            roleRepository.findByIdOrNull(postId) ?: throw PostNotFoundException(
                details = mapOf("postId" to postId),
            )

        val userEntity =
            userRepository.findByIdOrNull(userId)
                ?: throw UserNotFoundException(
                    details = mapOf("userId" to userId),
                )

        if (bookmarkRepository.existsByUserAndRole(userEntity, roleEntity)) {
            throw PostAlreadyBookmarkedException(
                details = mapOf("userId" to userId, "postId" to postId),
            )
        }

        bookmarkRepository.save(
            BookmarkEntity(
                role = roleEntity,
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
        val roleEntity =
            roleRepository.findByIdOrNull(postId) ?: throw PostNotFoundException(
                details = mapOf("postId" to postId),
            )

        val userEntity =
            userRepository.findByIdOrNull(userId)
                ?: throw UserNotFoundException(
                    details = mapOf("userId" to userId),
                )

        if (!bookmarkRepository.existsByUserAndRole(userEntity, roleEntity)) {
            throw PostBookmarkNotFoundException(
                details = mapOf("userId" to userId, "postId" to postId),
            )
        }
        bookmarkRepository.deleteByUserAndRole(userEntity, roleEntity)
    }

    /**
     * Retrieves a paginated list of bookmarks for a specific user.
     *
     * @param userId The unique identifier of the user.
     * @param page The page number to retrieve.
     * @return A paginated [Page] of [RoleEntity] representing bookmarked posts.
     * @throws UserNotFoundException If the user does not exist.
     */
    @Transactional(readOnly = true)
    fun getBookmarks(
        userId: String,
        page: Int,
    ): Page<RoleEntity> {
        val userEntity =
            userRepository.findByIdOrNull(userId)
                ?: throw UserNotFoundException(
                    details = mapOf("userId" to userId),
                )

        val pageable = PageRequest.of(page, pageSize)

        // Fetch all bookmark Role IDs
        val bookmarkIds = bookmarkRepository.findAllByUser(userEntity).map { it.role.id }

        // Fetch paginated RoleEntities
        return roleRepository.findAllByIdIn(bookmarkIds, pageable)
    }

    /**
     * Creates dummy posts for testing or development purposes.
     *
     * @param cnt The number of dummy posts to create.
     */
    @Transactional
    fun makeDummyPosts(cnt: Int) {
        (1..cnt).forEach { index ->
            val admin: UserEntity =
                userRepository.findByLocalLoginId("dummy$index")
                    ?: userRepository.save(
                        UserEntity(
                            name = "dummy$index",
                            localLoginId = "dummy$index",
                            localLoginPasswordHash = BCrypt.hashpw("DummyPW$index!99", BCrypt.gensalt()),
                            userRole = UserRole.CURATOR,
                            snuMail = null,
                        ),
                    )

            val tags =
                listOf("Tech", "Finance", "Health", "Ambient", "Salary")
                    .shuffled()
                    .take((1..3).random())
                    .map { Tag(it) }
                    .toMutableList()

            val companies = listOf("Company A$index", "Company B$index", "Company C$index").joinToString(", ")

            val companyEntity: CompanyEntity =
                companyRepository.save(
                    CompanyEntity(
                        admin = admin,
                        companyName = "dummy Company $index",
                        explanation = "Explanation of dummy Company $index",
                        email = "dummy$index@example.com",
                        slogan = "Slogan of dummy$index",
                        investAmount = (1000..5000).random(),
                        investCompany = companies,
                        series = Series.entries.random(),
                        imageLink = "https://www.company$index.dummy$index/image",
                        irDeckLink = "https://www.company$index.dummy$index/IRDECK",
                        landingPageLink = "https://www.company$index.dummy$index/LandingPage",
                        tags = tags,
                        links =
                            mutableListOf(
                                Link(
                                    link = "https://example.com/$index/link1",
                                    description = "Link 1",
                                ),
                                Link(
                                    link = "https://example.com/$index/link2",
                                    description = "Link 2",
                                ),
                            ),
                    ),
                )

            Category.entries.shuffled().take((1..3).random()).forEach { category ->
                val roleEntity =
                    roleRepository.save(
                        RoleEntity(
                            category = category,
                            detail = "Detail of $category",
                            headcount = "${(1..3).random()}",
                            isActive = true,
                            employmentEndDate = LocalDateTime.now().plusHours((-15..15).random().toLong()),
                            company = companyEntity,
                        ),
                    )
                companyEntity.roles += roleEntity
            }
        }
    }

    /**
     * Resets the database by deleting all companies, bookmarks, and roles.
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
        roleRepository.deleteAll()
    }

    companion object {
        private const val DEFAULT_PAGE_SIZE = 12
    }

    @Value("\${custom.SECRET}")
    private lateinit var resetDbSecret: String
}
