package com.waffletoy.team1server.post.persistence

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "INVEST_COMPANY")
open class InvestCompanyEntity(
    @Id
    @Column(name = "ID", nullable = false)
    open val id: String = UUID.randomUUID().toString(),
    @Column(name = "COMPANY_NAME")
    val companyName: String,
    // 다대일 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    // FK 컬럼 설정
    @JoinColumn(name = "POST_ENTITY", nullable = false)
    // 순환 참조 방지
    @JsonBackReference
    val postEntity: PostEntity? = null,
)
