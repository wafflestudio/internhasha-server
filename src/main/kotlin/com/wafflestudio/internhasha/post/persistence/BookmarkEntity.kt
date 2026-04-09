package com.wafflestudio.internhasha.post.persistence

import com.wafflestudio.internhasha.applicant.persistence.ApplicantEntity
import com.wafflestudio.internhasha.common.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "bookmarks",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["applicant_id", "position_id"]),
    ],
)
class BookmarkEntity(
    applicantEntity: ApplicantEntity,
    positionEntity: PositionEntity,
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "bookmark_id")
    var id: String? = null
        protected set

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id")
    var applicant: ApplicantEntity = applicantEntity
        protected set

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "position_id")
    var position: PositionEntity = positionEntity
        protected set
}
