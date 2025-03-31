package com.waffletoy.team1server.company.persistence

import com.waffletoy.team1server.auth.persistence.UserEntity
import com.waffletoy.team1server.company.dto.LinkVo
import com.waffletoy.team1server.company.dto.TagVo
import com.waffletoy.team1server.post.persistence.PositionEntity
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "companies")
@EntityListeners(AuditingEntityListener::class)
class CompanyEntity(
    @Id
    @Column(name = "ID", nullable = false)
    open val id: String = UUID.randomUUID().toString(),
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER", nullable = false)
    open val user: UserEntity,
    @Column(name = "ESTABLISHED_YEAR")
    open var companyEstablishedYear: Int? = null,
    @Column(name = "DOMAIN")
    open var domain: String? = null,
    @Column(name = "HEADCOUNT")
    open var headcount: Int? = null,
    @Column(name = "LOCATION")
    open var location: String? = null,
    @Column(name = "SLOGAN")
    open var slogan: String? = null,
    @Column(name = "DETAIL", columnDefinition = "TEXT")
    open var detail: String? = null,
    @Column(name = "PROFILE_IMAGE_KEY")
    open var profileImageKey: String? = null,
    @Column(name = "COMPANY_INFO_PDF_LINK", length = 2048)
    open var companyInfoPDFLink: String? = null,
    @Column(name = "LANDING_PAGE_LINK", length = 2048)
    open var landingPageLink: String? = null,
    @Column(name = "VC_NAME")
    open var vcName: String? = null,
    @Column(name = "VC_REC")
    open var vcRec: String? = null,
    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    open var createdAt: LocalDateTime = LocalDateTime.now(),
    @LastModifiedDate
    @Column(name = "UPDATED_AT", nullable = false)
    open var updatedAt: LocalDateTime = LocalDateTime.now(),
    // 링크를 value object로 관리 - 자동으로 테이블 생성
    @ElementCollection
    @CollectionTable(
        // 매핑될 테이블 이름
        name = "company_links",
        joinColumns = [JoinColumn(name = "company_id")],
    )
    open var links: MutableList<LinkVo> = mutableListOf(),
    // 태그를 value object로 관리 - 자동으로 테이블 생성
    @ElementCollection
    @CollectionTable(
        // 매핑될 테이블 이름
        name = "company_tags",
        joinColumns = [JoinColumn(name = "company_id")],
    )
    open var tags: MutableList<TagVo> = mutableListOf(),
    // Positions 테이블의 POST 외래 키를 매핑
    @OneToMany(mappedBy = "company", cascade = [CascadeType.ALL], orphanRemoval = true)
    open val positions: MutableList<PositionEntity> = mutableListOf(),
)
