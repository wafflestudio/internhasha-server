package com.wafflestudio.internhasha.post.persistence

import com.wafflestudio.internhasha.applicant.persistence.ApplicantRepository
import com.wafflestudio.internhasha.post.application.port.BookmarkPersistencePort
import com.wafflestudio.internhasha.post.domain.Bookmark
import com.wafflestudio.internhasha.post.domain.Position
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class BookmarkPersistenceAdapter(
    private val bookmarkRepository: BookmarkRepository,
    private val applicantRepository: ApplicantRepository,
    private val positionRepository: PositionRepository,
    private val positionMapper: PositionPersistenceMapper,
) : BookmarkPersistencePort {
    override fun findBookmarkByApplicantAndPositionId(
        applicantId: String,
        positionId: String,
    ): Bookmark? {
        return bookmarkRepository.findByApplicantIdAndPositionId(applicantId, positionId)?.toDomain()
    }

    override fun existsByApplicantIdAndPositionId(
        applicantId: String,
        positionId: String,
    ): Boolean {
        return bookmarkRepository.existsByApplicantIdAndPositionId(applicantId, positionId)
    }

    override fun getAllBookmarkByApplicantId(applicantId: String): List<Bookmark> {
        return bookmarkRepository.findAllByApplicantId(applicantId)
            .map { it.toDomain() }
    }

    override fun getPositionsPageBookmarkedByApplicationId(
        applicantId: String,
        pageable: Pageable,
    ): Page<Position> {
        return bookmarkRepository.findAllByApplicantIdWithFetchJoin(
            applicantId = applicantId,
            pageable = pageable,
        ).map { it.toPosition() }
    }

    @Transactional
    override fun createBookmark(bookmark: Bookmark): Bookmark {
        return bookmarkRepository.save(
            BookmarkEntity(
                applicantEntity = applicantRepository.getReferenceById(bookmark.applicantId),
                positionEntity = positionRepository.getReferenceById(bookmark.positionId),
            ),
        ).toDomain()
    }

    @Transactional
    override fun deleteBookmark(bookmark: Bookmark) {
        bookmarkRepository.deleteById(bookmark.id)
    }

    private fun BookmarkEntity.toDomain(): Bookmark {
        return Bookmark(
            id = requireNotNull(this.id) { "Bookmark id can not be null" },
            applicantId = requireNotNull(this.applicant.id) { "Bookmark applicant id can not be null" },
            positionId = requireNotNull(this.position.id) { "Bookmark position id can not be null" },
        )
    }

    private fun BookmarkEntity.toPosition(): Position {
        return positionMapper.toDomain(this.position)
    }
}
