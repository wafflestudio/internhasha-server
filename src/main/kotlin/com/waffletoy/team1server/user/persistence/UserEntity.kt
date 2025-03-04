package com.waffletoy.team1server.user.persistence

import com.waffletoy.team1server.user.UserRole
import jakarta.persistence.*
import jakarta.validation.ValidationException
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

// TODO: UserEntity is not scalable to any social logins.
@Entity(name = "users")
@EntityListeners(AuditingEntityListener::class)
class UserEntity(
    // Basic user information
    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    val id: String = UUID.randomUUID().toString(),
    @Column(name = "name", nullable = false)
    var name: String,
    // Login credentials(local or google)
    @Column(name = "local_login_id", unique = true, nullable = true)
    var localLoginId: String? = null,
    @Column(name = "local_login_password_hash", nullable = true)
    open var localLoginPasswordHash: String? = null,
    // Date info
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null,
    // Role-based access control
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val userRole: UserRole,
    // APPLICATNT specific field
    @Column(name = "snu_mail", nullable = true, unique = true)
    val snuMail: String?,
    @Column(name = "phone_number", nullable = true)
    var phoneNumber: String? = null,
    @Column(name = "profile_image_link", nullable = true, length = 2048)
    val profileImageLink: String? = null,
) {
    // Validates either local or google login is provided
    @PrePersist
    @PreUpdate
    fun validate() {
        if ((localLoginId == null || localLoginPasswordHash == null)) {
            throw ValidationException("Either localLoginId and passwordHash must both be non-null or googleLoginId must be non-null.")
        }
        if ((userRole == UserRole.NORMAL) && snuMail == null) {
            throw ValidationException("snuMail must not be null for APPLICANT.")
        }
    }
}
