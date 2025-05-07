package com.wafflestudio.internhasha.post.persistence

import com.wafflestudio.internhasha.company.persistence.CompanyEntity
import com.wafflestudio.internhasha.post.Category
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "positions")
@EntityListeners(AuditingEntityListener::class)
class PositionEntity(
    @Id
    @Column(name = "ID", nullable = false)
    open val id: String = UUID.randomUUID().toString(),
    @Column(name = "POSITION_TITLE", nullable = false)
    open var positionTitle: String = "",
    @Column(name = "POSITION_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    open var positionType: Category,
    @Column(name = "DETAIL", nullable = false, columnDefinition = "TEXT")
    open var detail: String,
    @Column(name = "HEADCOUNT", nullable = false)
    open var headCount: Int = 0,
    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false)
    open var createdAt: LocalDateTime = LocalDateTime.now(),
    @LastModifiedDate
    @Column(name = "UPDATED_AT", nullable = false)
    open var updatedAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "EMPLOYMENT_END_DATE", nullable = true)
    open var employmentEndDate: LocalDateTime? = null,
    @Column(name = "SALARY", nullable = true)
    open var salary: Int? = null,
    // 특정 Company에 join
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "COMPANY_ID", nullable = false)
    open val company: CompanyEntity,
)
