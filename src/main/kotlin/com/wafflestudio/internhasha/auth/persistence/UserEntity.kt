package com.wafflestudio.internhasha.auth.persistence

import com.wafflestudio.internhasha.auth.domain.UserRole
import com.wafflestudio.internhasha.common.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import org.mindrot.jbcrypt.BCrypt
import java.time.LocalDateTime

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "user_role")
abstract class UserEntity(
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

    fun authenticate(rawPassword: String): Boolean {
        return passwordHash?.let { BCrypt.checkpw(rawPassword, it) } ?: false
    }
}
