package com.wafflestudio.internhasha.post.domain

import com.wafflestudio.internhasha.common.domain.BaseEntity
import com.wafflestudio.internhasha.company.domain.Company
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "positions")
class Position(
    var positionTitle: String,
    @Enumerated(EnumType.STRING)
    var positionType: Category,
    @Column(columnDefinition = "TEXT")
    var detail: String,
    var headCount: Long = 0,
    var employmentEndDate: LocalDateTime? = null,
    var salary: Long? = null,
    company: Company,
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "position_id")
    var id: String? = null
        protected set

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id")
    var company: Company = company
        protected set

    fun requireId(): String = requireNotNull(id) { "Entity Id must not be null" }
}
