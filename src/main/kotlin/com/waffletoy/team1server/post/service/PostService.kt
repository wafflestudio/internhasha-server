package com.waffletoy.team1server.post.service

import com.waffletoy.team1server.account.controller.User
import com.waffletoy.team1server.post.PostServiceException
import com.waffletoy.team1server.post.controller.Post
import com.waffletoy.team1server.post.controller.PostBrief
import com.waffletoy.team1server.post.persistence.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PostService(
    private val postRepository: PostRepository,
    private val bookmarkRepository: BookmarkRepository
) {

    fun getPageDetail(postId: String): Post {
        val postEntity = postRepository.findByIdOrNull(postId)?: throw PostServiceException(
            "해당 ID를 가진 포스트가 없습니다.",
            HttpStatus.NOT_FOUND
        )
        return Post.fromEntity(postEntity)
    }

    fun getPosts(
        roles: List<String>?,
        investment: Int?,
        investor: List<String>?,
        status: Int?,
        page: Int = 0
    ): Page<PostEntity> {
        val specification = PostSpecification.withFilters(
            roles, investment, investor, status?:2
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
                HttpStatus.CONFLICT
            )
        }
        val post = postRepository.findByIdOrNull(postId) ?: throw PostServiceException(
            "존재하지 않는 채용정보입니다.",
            HttpStatus.NOT_FOUND,
        )
        val bookmarkEntity = bookmarkRepository.save(
            BookmarkEntity(
                postId = post.id,
                userId = user.id,
            )
        )
        return
    }

    @Transactional
    fun deleteBookmark(
        user: User,
        postId: String,
    ) {
        if (!bookmarkRepository.existsByUserIdAndPostId(user.id, postId)){
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
    ) : Page<PostEntity> {
        val pageable = PageRequest.of(page, pageSize)

        // 북마크 ID 가져오기
        val bookmarkIds = bookmarkRepository.findAllByUserId(user.id).map { it.postId }

        // PostEntity를 페이징 처리하여 가져오기
        return postRepository.findAllByIdIn(bookmarkIds, pageable)
    }

    private val pageSize = 12
}