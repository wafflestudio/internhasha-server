package com.wafflestudio.internhasha.post.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface BookmarkRepository : JpaRepository<BookmarkEntity, String> {
    fun findByApplicantIdAndPositionId(
        applicantId: String,
        positionId: String,
    ): BookmarkEntity?

    fun existsByApplicantIdAndPositionId(
        applicantId: String,
        positionId: String,
    ): Boolean

    fun findAllByApplicantId(applicantId: String): List<BookmarkEntity>

    @Query(
        """
        select b from BookmarkEntity b
        join fetch b.position p
        join fetch p.company c
        where b.applicant.id = :applicantId
    """,
    )
    fun findAllByApplicantIdWithFetchJoin(
        applicantId: String,
        pageable: Pageable,
    ): Page<BookmarkEntity>

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM BookmarkEntity b WHERE b.user.id = :user")
    fun deleteAllByUserId(userId: String)
}
