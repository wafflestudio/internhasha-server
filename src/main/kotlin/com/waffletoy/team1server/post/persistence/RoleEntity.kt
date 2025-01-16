package com.waffletoy.team1server.post.persistence

import com.waffletoy.team1server.post.Category
import com.waffletoy.team1server.resume.persistence.ResumeEntity
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "roles")
class RoleEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false)
    open val id: String = UUID.randomUUID().toString(),
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
    @Column(name = "EMPLOYMENT_END_DATE", nullable = false)
    open var employmentEndDate: LocalDateTime = LocalDateTime.now(),
    @Column(name = "IS_ACTIVE")
    open val isActive: Boolean = false,
    // 특정 POST에 join
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "COMPANY_ID", nullable = false)
    open val company: CompanyEntity,
    // ResumeEntity의 role 필드와 join
    @OneToMany(mappedBy = "role", cascade = [CascadeType.ALL], orphanRemoval = true)
    open val resumes: List<ResumeEntity> = emptyList(),
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
