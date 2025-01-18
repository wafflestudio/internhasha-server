package com.waffletoy.team1server.post.persistence

import com.waffletoy.team1server.post.Series
import com.waffletoy.team1server.user.persistence.UserEntity
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "companies")
class CompanyEntity(
    @Id
    @Column(name = "ID", nullable = false)
    open val id: String = UUID.randomUUID().toString(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ADMIN", nullable = false)
    open val admin: UserEntity,
    @Column(name = "NAME", nullable = false)
    open var companyName: String,
    @Column(name = "EXPLANATION", columnDefinition = "TEXT")
    open var explanation: String? = null,
    @Column(name = "EMAIL")
    open var email: String? = null,
    @Column(name = "SLOGUN")
    open var slogan: String? = null,
    @Column(name = "INVEST_AMOUNT")
    open var investAmount: Int = 0,
    @Column(name = "INVEST_COMPANY")
    open var investCompany: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "SERIES", nullable = false)
    open var series: Series = Series.SEED,
    @Column(name = "IMAGE_LINK")
    open var imageLink: String? = null,
    @Column(name = "IR_DECK_LINK")
    open var irDeckLink: String? = null,
    @Column(name = "LANDING_PAGE_LINK")
    open var landingPageLink: String? = null,
    @Column(name = "CREATED_AT", nullable = false)
    open var createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "UPDATED_AT", nullable = false)
    open var updatedAt: LocalDateTime = LocalDateTime.now(),
    // Links 테이블에서 POST_ID를 외래 키로 사용
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "POST_ID")
    open val links: MutableList<LinkEntity> = mutableListOf(),
    // TAGS 테이블의 POST를 외래키로 사용
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "POST_ID")
    open val tags: MutableList<TagEntity> = mutableListOf(),
    // ROLES 테이블의 POST 외래 키를 매핑
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "COMPANY_ID")
    open val roles: MutableList<RoleEntity> = mutableListOf(),
) {
    @PrePersist
    fun onCreate() {
        createdAt = LocalDateTime.now()
        updatedAt = LocalDateTime.now()
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
