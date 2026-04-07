package com.wafflestudio.internhasha.auth.persistence

import com.wafflestudio.internhasha.auth.UserRole
import com.wafflestudio.internhasha.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "user_role")
abstract class UserEntity(
    @Column(nullable = false)
    var name: String,
    @Column(nullable = false, unique = true)
    val email: String,
    @Column(nullable = true)
    val passwordHash: String? = null,
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    var id: String? = null
        protected set

    // Role-based access control
    abstract fun getRole(): UserRole
}
