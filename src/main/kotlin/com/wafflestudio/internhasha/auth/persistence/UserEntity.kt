package com.wafflestudio.internhasha.auth.persistence

import com.wafflestudio.internhasha.applicant.persistence.ApplicantEntity
import com.wafflestudio.internhasha.auth.UserRole
import com.wafflestudio.internhasha.company.persistence.CompanyEntity
import jakarta.persistence.*
import jakarta.validation.ValidationException
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@Entity(name = "users")
@EntityListeners(AuditingEntityListener::class)
class UserEntity(
    // Basic user information
    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    val id: String = UUID.randomUUID().toString(),
    @Column(name = "name", nullable = false)
    var name: String,
    @Column(name = "email", nullable = false, unique = true)
    val email: String,
    @Column(name = "password_hash", nullable = true)
    open var passwordHash: String? = null,
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
    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, optional = true)
    var company: CompanyEntity? = null,
    @Deprecated("This field will be moved to ApplicantEntity, CompanyEntity respectively.")
    @Column(name = "profile_image_link", nullable = true, length = 2048)
    val profileImageLink: String? = null,
    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, optional = true)
    val applicant: ApplicantEntity? = null,
    // APPLICATNT specific field
//    @Column(name = "phone_number", nullable = true)
//    var phoneNumber: String? = null,,
) {
    // Validates either local or google login is provided
    @PrePersist
    @PreUpdate
    fun validate() {
        if (passwordHash == null) {
            throw ValidationException("passwordHash must be non-null")
        }
        if (userRole == UserRole.APPLICANT && name.isNullOrBlank()) {
            throw ValidationException("User name cannot be blank")
        }
    }
}
