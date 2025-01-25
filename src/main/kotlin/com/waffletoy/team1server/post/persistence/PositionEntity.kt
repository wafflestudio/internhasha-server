package com.waffletoy.team1server.post.persistence

import com.waffletoy.team1server.post.Category
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "positions")
class PositionEntity(
    @Id
    @Column(name = "ID", nullable = false)
    open val id: String = UUID.randomUUID().toString(),
    @Column(name = "TITLE", nullable = false)
    open val title: String = "",
    @Column(name = "CATEGORY", nullable = false)
    @Enumerated(EnumType.STRING)
    open var category: Category,
    @Column(name = "DETAIL", nullable = true, columnDefinition = "TEXT")
    open var detail: String? = null,
    @Column(name = "HEADCOUNT", nullable = false)
    open var headcount: String,
    @Column(name = "CREATED_AT", nullable = false)
    open var createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "UPDATED_AT", nullable = false)
    open var updatedAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "EMPLOYMENT_END_DATE", nullable = true)
    open var employmentEndDate: LocalDateTime? = null,
    @Column(name = "IS_ACTIVE")
    open val isActive: Boolean = false,
    // 특정 Company에 join
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "COMPANY_ID", nullable = false)
    open val company: CompanyEntity,
) {
    @PrePersist
    fun onCreate() {
        createdAt = LocalDateTime.now()
        updatedAt = createdAt
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
