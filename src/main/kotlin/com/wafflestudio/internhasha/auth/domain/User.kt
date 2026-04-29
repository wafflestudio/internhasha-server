package com.wafflestudio.internhasha.auth.domain

import com.wafflestudio.internhasha.common.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "user_role")
abstract class User(
    @Column(nullable = false)
    var name: String,
    @Column(nullable = false, unique = true)
    var email: String,
    @Column(nullable = true)
    var passwordHash: String? = null,
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    var id: String? = null
        protected set

    var deletedAt: LocalDateTime? = null
        protected set

    fun deleteUser() {
        if (deletedAt == null) {
            passwordHash = null
            deletedAt = LocalDateTime.now()
            email = "$email#deleted$id"
        }
    }

    // Role-based access control
    abstract fun getRole(): UserRole

    fun requireId(): String = requireNotNull(id) { "Entity Id must not be null" }
}
