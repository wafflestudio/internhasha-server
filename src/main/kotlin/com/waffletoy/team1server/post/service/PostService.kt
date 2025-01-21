package com.waffletoy.team1server.post.service

import com.waffletoy.team1server.post.Category
import com.waffletoy.team1server.post.PostServiceException
import com.waffletoy.team1server.post.Series
import com.waffletoy.team1server.post.controller.Post
import com.waffletoy.team1server.post.persistence.*
import com.waffletoy.team1server.user.Role
import com.waffletoy.team1server.user.persistence.*
import com.waffletoy.team1server.user.service.UserService
import org.mindrot.jbcrypt.BCrypt
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PostService(
    private val companyRepository: CompanyRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val userService: UserService,
    private val tagRepository: TagRepository,
    private val roleRepository: RoleRepository,
    private val userRepository: UserRepository,
) {
    fun getPageDetail(postId: String): Post {
        val postEntity =
            roleRepository.findByIdOrNull(postId) ?: throw PostServiceException(
                "해당 ID를 가진 포스트가 없습니다.",
                HttpStatus.NOT_FOUND,
            )
        return Post.fromEntity(postEntity)
    }

    fun getPosts(
        roles: List<String>?,
        investmentMax: Int?,
        investmentMin: Int?,
        status: Int?,
        series: List<String>?,
        page: Int = 0,
    ): Page<RoleEntity> {
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

        // PostEntity를 페이징 처리하여 가져오기
        return roleRepository.findAllByIdIn(roleIds, pageable)
    }

    @Transactional
    fun bookmarkPost(
        userId: String,
        postId: String,
    ) {
        val roleEntity =
            roleRepository.findByIdOrNull(postId) ?: throw PostServiceException(
                "존재하지 않는 채용정보입니다.",
                HttpStatus.NOT_FOUND,
            )

        val userEntity =
            userRepository.findByIdOrNull(userId)
                ?: throw PostServiceException("유저를 찾을 수 없습니다.")

        if (bookmarkRepository.existsByUserAndRole(userEntity, roleEntity)) {
            throw PostServiceException(
                "이미 북마크에 추가된 포스트입니다.",
                HttpStatus.CONFLICT,
            )
        }

        bookmarkRepository.save(
            BookmarkEntity(
                role = roleEntity,
                user = userEntity,
            ),
        )
        return
    }

    @Transactional
    fun deleteBookmark(
        userId: String,
        postId: String,
    ) {
        val roleEntity =
            roleRepository.findByIdOrNull(postId) ?: throw PostServiceException(
                "존재하지 않는 채용정보입니다.",
                HttpStatus.NOT_FOUND,
            )

        val userEntity =
            userRepository.findByIdOrNull(userId)
                ?: throw PostServiceException("유저를 찾을 수 없습니다.")

        if (!bookmarkRepository.existsByUserAndRole(userEntity, roleEntity)) {
            throw PostServiceException(
                "존재하지 않는 북마크입니다",
                HttpStatus.NOT_FOUND,
            )
        }
        bookmarkRepository.deleteByUserAndRole(userEntity, roleEntity)
        return
    }

    @Transactional(readOnly = true)
    fun getBookmarks(
        userId: String,
        page: Int,
    ): Page<RoleEntity> {
        val userEntity =
            userRepository.findByIdOrNull(userId)
                ?: throw PostServiceException("유저를 찾을 수 없습니다.")

        val pageable = PageRequest.of(page, pageSize)

        // 북마크 ID 가져오기
        val bookmarkIds = bookmarkRepository.findAllByUser(userEntity).map { it.id }

        // PostEntity를 페이징 처리하여 가져오기
        return roleRepository.findAllByIdIn(bookmarkIds, pageable)
    }

    @Transactional
    fun makeDummyPosts(cnt: Int) {
        (1..cnt).forEach {
            val admin: UserEntity =
                userRepository.findByLocalLoginId("dummy$it")
                    ?: userRepository.save(
                        UserEntity(
                            name = "dummy$it",
                            localLoginId = "dummy$it",
                            localLoginPasswordHash = BCrypt.hashpw("DummyPW$it!99", BCrypt.gensalt()),
                            role = Role.ROLE_POST_ADMIN,
                            snuMail = null,
                        ),
                    )

            val tags =
                listOf("Tech", "Finance", "Health", "Ambient", "Salary")
                    .shuffled()
                    .take((1..3).random())
                    .map { it2 ->
                        TagEntity(
                            tag = it2,
                        )
                    }
                    .toMutableList()

            val companies = listOf("Company A$it", "Company B$it", "Company C$it").joinToString(", ")

            val companyEntity: CompanyEntity =
                companyRepository.save(
                    CompanyEntity(
                        admin = admin,
                        companyName = "dummy Company $it",
                        explanation = "explanation of dummy Company $it",
                        email = "dummy$it@example.com",
                        slogan = "slogan of dummy$it",
                        investAmount = (1000..5000).random(),
                        investCompany = companies,
                        series = Series.entries.random(),
                        imageLink = "www.company$it.dummy$it/image",
                        irDeckLink = "www.company$it.dummy$it/IRDECK",
                        landingPageLink = "www.company$it.dummy$it/LandingPage",
                        tags = tags,
                        links =
                            mutableListOf(
                                LinkEntity(
                                    link = "https://example.com/$it/link1",
                                    description = "link1",
                                ),
                                LinkEntity(
                                    link = "https://example.com/$it/link2",
                                    description = "link2",
                                ),
                            ),
                    ),
                )

            Category.entries.shuffled().take((1..3).random()).map { it2 ->
                val roleEntity =
                    roleRepository.save(
                        RoleEntity(
                            category = it2,
                            detail = "detail of $it2",
                            headcount = "${(1..3).random()}",
                            isActive = true,
                            employmentEndDate = LocalDateTime.now().plusHours((-15..15).random().toLong()),
                            company = companyEntity,
                        ),
                    )
                companyEntity.roles += roleEntity
                roleEntity
            }
        }
    }

    fun resetDB() {
        companyRepository.deleteAll()
        tagRepository.deleteAll()
        bookmarkRepository.deleteAll()
        roleRepository.deleteAll()
    }

    private val pageSize = 12
}
