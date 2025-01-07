package com.waffletoy.team1server.post.persistence

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "roles")
open class RoleEntity(
    @Id
    @Column(name = "ID", nullable = false)
    open val id: String = UUID.randomUUID().toString(),

    @Column(name = "category", nullable = false)
    @Enumerated(EnumType.STRING) // ENUM 타입으로 지정
    val category: Category,

    @Column(name = "detail", nullable = true)
    val detail: String? = null,

    @Column(name = "headcount", nullable = false)
    val headcount: String
)

// ENUM 타입 정의
enum class Category {
    CATEGORY_A, // 적절한 값을 여기에 추가
    CATEGORY_B,
    CATEGORY_C
}
