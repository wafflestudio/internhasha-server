package com.waffletoy.team1server.pretotype.persistence

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "pretotypes")
class PretotypeEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(unique = true)
    var email: String = "",
    @Column(name = "is_subscribed")
    var isSubscribed: Boolean = false,
    @Column(name = "created_at")
    var createdAt: Instant = Instant.now(),
) {
    // No-args constructor for Hibernate
    constructor() : this(null, "", false, Instant.now())
}
