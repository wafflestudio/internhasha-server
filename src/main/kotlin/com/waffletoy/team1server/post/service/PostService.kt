package com.waffletoy.team1server.post.service

import com.waffletoy.team1server.account.UserServiceException
import com.waffletoy.team1server.account.controller.User
import com.waffletoy.team1server.account.persistence.*
import com.waffletoy.team1server.account.service.UserService
import com.waffletoy.team1server.post.Category
import com.waffletoy.team1server.post.PostServiceException
import com.waffletoy.team1server.post.controller.Post
import com.waffletoy.team1server.post.persistence.*
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
    private val postRepository: PostRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val userService: UserService,
    private val accountRepository: AccountRepository,
    private val tagRepository: TagRepository,
    private val roleRepository: RoleRepository,
) {
    fun getPageDetail(postId: String): Post {
        val postEntity =
            postRepository.findByIdOrNull(postId) ?: throw PostServiceException(
                "해당 ID를 가진 포스트가 없습니다.",
                HttpStatus.NOT_FOUND,
            )
        return Post.fromEntity(postEntity)
    }

    fun getPosts(
        roles: List<String>?,
        investmentUp: Int?,
        investmentDown: Int?,
        status: Int?,
        page: Int = 0,
    ): Page<PostEntity> {
        val specification =
            PostSpecification.withFilters(
                roles,
                investmentUp,
                investmentDown,
                status ?: 2,
            )

        val pageable = PageRequest.of(page, pageSize)

        val postIds = postRepository.findAll(specification).map { it.id }

        // PostEntity를 페이징 처리하여 가져오기
        return postRepository.findAllByIdIn(postIds, pageable)

//        return posts.map { PostBrief.fromPost(Post.fromEntity(it)) }
    }

    @Transactional
    fun bookmarkPost(
        user: User,
        postId: String,
    ) {
        if (bookmarkRepository.existsByUserIdAndPostId(user.id, postId)) {
            throw PostServiceException(
                "이미 북마크에 추가된 포스트입니다.",
                HttpStatus.CONFLICT,
            )
        }
        val post =
            postRepository.findByIdOrNull(postId) ?: throw PostServiceException(
                "존재하지 않는 채용정보입니다.",
                HttpStatus.NOT_FOUND,
            )
        val bookmarkEntity =
            bookmarkRepository.save(
                BookmarkEntity(
                    postId = post.id,
                    userId = user.id,
                ),
            )
        return
    }

    @Transactional
    fun deleteBookmark(
        user: User,
        postId: String,
    ) {
        if (!bookmarkRepository.existsByUserIdAndPostId(user.id, postId)) {
            throw PostServiceException(
                "존재하지 않는 북마크입니다",
                HttpStatus.NOT_FOUND,
            )
        }
        bookmarkRepository.deleteByUserIdAndPostId(user.id, postId)
        return
    }

    @Transactional(readOnly = true)
    fun getBookmarks(
        user: User,
        page: Int,
    ): Page<PostEntity> {
        val pageable = PageRequest.of(page, pageSize)

        // 북마크 ID 가져오기
        val bookmarkIds = bookmarkRepository.findAllByUserId(user.id).map { it.postId }

        // PostEntity를 페이징 처리하여 가져오기
        return postRepository.findAllByIdIn(bookmarkIds, pageable)
    }

    @Transactional
    fun makeDummyPosts(cnt: Int) {
        (1..cnt).forEach {
            val admin: AdminEntity =
                if (accountRepository.existsByLocalId("dummy$it")) {
                    accountRepository.findByLocalId("dummy$it") as? AdminEntity ?: throw UserServiceException()
                } else {
                    accountRepository.save(
                        AdminEntity(
                            username = "dummy$it",
                            localId = "dummy$it",
                            password = BCrypt.hashpw("DummyPW$it!99", BCrypt.gensalt()),
                        ),
                    )
                }

            val roles =
                listOf("PLANNER", "FRONT", "BACKEND").map { it2 ->
                    roleRepository.save(
                        RoleEntity(
                            category = Category.entries.find { it3 -> it3.name == it2 } ?: Category.DESIGN,
                            detail = it2,
                            headcount = "$it",
                        ),
                    )
                }

            val companies = listOf("Company A$it", "Company B$it", "Company C$it").joinToString(", ")

            val tags =
                listOf("Tech", "Finance", "Health").map { tagName ->
                    tagRepository.findByTag(tagName) ?: // 태그가 이미 있는지 확인
                        tagRepository.save(TagEntity(tag = tagName))
                }

            val post =
                PostEntity(
                    admin =
                        admin as? AdminEntity
                            ?: throw IllegalStateException("Admin is not an instance of AdminEntity"),
                    title = "Post Title $it",
                    companyName = "Company $it",
                    explanation = "This is explanation for post $it",
                    content = "This is content for post $it",
                    email = "contact$it@company.com",
                    imageLink = "https://example.com/image$it.jpg",
                    investAmount = (1000..5000).random(),
                    investCompany = companies,
                    roles = roles.shuffled().take((1..2).random()).toMutableList(),
                    tags = tags.shuffled().take((1..3).random()).toMutableSet(),
                    isActive = it % 2 == 0,
                    employmentEndDate = LocalDateTime.now().plusDays((1..30).random().toLong()),
                    links =
                        listOf(
                            LinkEntity(link = "https://example.com/$it/link1"),
                            LinkEntity(link = "https://example.com/$it/link2"),
                        ),
                )
            postRepository.save(post)
        }
    }

    fun resetDB() {
        postRepository.deleteAll()
        tagRepository.deleteAll()
        roleRepository.deleteAll()
    }

    private val pageSize = 12
}
