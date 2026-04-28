package com.wafflestudio.internhasha.post.repository

import com.wafflestudio.internhasha.post.domain.Bookmark
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BookmarkRepository : JpaRepository<Bookmark, String> {
    fun findByApplicantIdAndPositionId(
        applicantId: String,
        positionId: String,
    ): Bookmark?

    fun existsByApplicantIdAndPositionId(
        applicantId: String,
        positionId: String,
    ): Boolean

    fun findAllByApplicantId(applicantId: String): List<Bookmark>

    @Query(
        """
        select b from Bookmark b
        join fetch b.position p
        join fetch p.company c
        where b.applicant.id = :applicantId
    """,
    )
    fun findAllByApplicantIdWithFetchJoin(
        applicantId: String,
        pageable: Pageable,
    ): Page<Bookmark>

    fun deleteAllByApplicantId(applicantId: String)
}
