package com.waffletoy.team1server.post.service

import com.waffletoy.team1server.account.controller.User
import com.waffletoy.team1server.post.PostServiceException
import com.waffletoy.team1server.post.controller.Post
import com.waffletoy.team1server.post.controller.PostBrief
import com.waffletoy.team1server.post.persistence.BookmarkEntity
import com.waffletoy.team1server.post.persistence.BookmarkRepository
import com.waffletoy.team1server.post.persistence.PostRepository
import com.waffletoy.team1server.post.persistence.PostSpecification
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
    ): List<PostBrief> {
        val specification = PostSpecification.withFilters(roles, investment, investor)
        val posts = postRepository.findAll(specification)
        return posts.map { PostBrief.fromPost(Post.fromEntity(it)) }
    }

    @Transactional
    fun bookmarkPost(
        user: User,
        postId: String,
    ) {
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
        val post = postRepository.findByIdOrNull(postId) ?: throw PostServiceException(
            "존재하지 않는 채용정보입니다.",
            HttpStatus.NOT_FOUND,
        )
        bookmarkRepository.deleteByUserIdAndPostId(user.id, postId)
        return
    }

    @Transactional(readOnly = true)
    fun getBookmarks(
        user: User,
        page: Int,
    ) : List<PostBrief> {
        val bookmarks = bookmarkRepository.findAllByUserId(user.id)
            .mapNotNull { bookmark ->
                postRepository.findByIdOrNull(bookmark.postId) // postId를 사용해 Post 엔티티를 찾음
            }

        // 페이지네이션 처리
        val paginatedPosts = bookmarks
            .drop(page * pageSize) // 현재 페이지까지 데이터를 건너뛰기
            .take(pageSize)       // 페이지 크기만큼 가져오기

        // PostBrief로 변환
        return paginatedPosts.map { post ->
            PostBrief.fromPost(Post.fromEntity(post))
        }
    }

    private val pageSize = 12
}