package com.wafflestudio.internhasha.post.domain

import com.wafflestudio.internhasha.applicant.domain.Applicant
import com.wafflestudio.internhasha.common.domain.BaseEntity
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
class Bookmark(
    applicant: Applicant,
    position: Position,
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "bookmark_id")
    var id: String? = null
        protected set

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id")
    var applicant: Applicant = applicant
        protected set

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "position_id")
    var position: Position = position
        protected set

    fun requireId(): String {
        return requireNotNull(id) { "Entity Id must not be null" }
    }
}
