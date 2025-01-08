package com.waffletoy.team1server.post.persistence

import com.fasterxml.jackson.annotation.JsonBackReference
import com.waffletoy.team1server.post.controller.Post
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

    @ManyToOne(fetch = FetchType.LAZY) // 다대일 관계 설정
    @JoinColumn(name = "POST_ENTITY", nullable = false) // FK 컬럼 설정
    @JsonBackReference // 순환 참조 방지
    val postEntity: PostEntity? = null
)