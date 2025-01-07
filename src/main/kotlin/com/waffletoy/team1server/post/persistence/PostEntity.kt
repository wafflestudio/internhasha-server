package com.waffletoy.team1server.post.persistence

import com.waffletoy.team1server.account.persistence.AdminEntity
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "POSTS")
open class PostEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: String = UUID.randomUUID().toString(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ADMIN_ID", nullable = false)
    open val admin: AdminEntity,

    @Column(name = "CREATED_AT", nullable = false)
    open val createdAt: LocalDateTime,

    @Column(name = "UPDATED_AT", nullable = false)
    open var updatedAt: LocalDateTime,

    @Column(name = "TITLE", nullable = false, length = 255)
    open val title: String,

    @Column(name = "companyName", nullable = false, length = 255)
    open val companyName: String,

    @Column(name = "EXPLANATION", columnDefinition = "TEXT")
    open val explanation: String? = null,

    @Column(name = "CONTENT", columnDefinition = "TEXT")
    open val content: String? = null,

    @Column(name = "EMAIL")
    open val email: String? = null,

    @Column(name = "IMAGE_LINK")
    open val imageLink: String? = null,

    @Column(name = "INVEST_AMOUNT")
    open val investAmount: Int? = null,

    @Column(name = "INVEST_COMPANY")
    open val investCompany: String? = null,

    @Column(name = "IR_DECK_LINK")
    open val irDeckLink: String? = null,

    @Column(name = "LANDING_PAGE_LINK")
    open val landingPageLink: String? = null,

    @Column(name = "EMPLOYMENT_END_DATE", nullable = false)
    open val employmentEndDate: LocalDateTime
)
