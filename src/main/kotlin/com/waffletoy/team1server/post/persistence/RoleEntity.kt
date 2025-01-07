package com.waffletoy.team1server.post.persistence

import com.waffletoy.team1server.post.Category
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "roles")
open class RoleEntity(
    @Id
    @Column(name = "ID", nullable = false)
    open val id: String = UUID.randomUUID().toString(),

    @Column(name = "CATEGORY", nullable = false)
    @Enumerated(EnumType.STRING)
    val category: Category,

    @Column(name = "DETAIL", nullable = true)
    val detail: String? = null,

    @Column(name = "HEADCOUNT", nullable = false)
    val headcount: String,
) {
    constructor() : this(
        id = UUID.randomUUID().toString(),
        category = Category.CATEGORY_A, // 기본값 설정
        detail = null,
        headcount = "0"
    )
}