package com.wafflestudio.internhasha.auth.persistence

import com.wafflestudio.internhasha.applicant.persistence.ApplicantEntity
import com.wafflestudio.internhasha.auth.application.port.UserPersistencePort
import com.wafflestudio.internhasha.auth.domain.User
import com.wafflestudio.internhasha.auth.domain.UserApplicant
import com.wafflestudio.internhasha.auth.domain.UserCompany
import com.wafflestudio.internhasha.auth.domain.UserNotFoundException
import com.wafflestudio.internhasha.company.persistence.CompanyEntity
import org.springframework.stereotype.Component

@Component
class UserPersistenceAdapter(
    private val userRepository: UserRepository,
) : UserPersistencePort {
    override fun findUserById(userId: String): User? {
        return userRepository.findByIdAndDeletedAtIsNull(userId)?.toUser()
    }

    override fun findUserByEmail(email: String): User? {
        return userRepository.findByEmailAndDeletedAtIsNull(email)?.toUser()
    }

    override fun changePassword(
        userId: String,
        newPasswordHash: String,
    ) {
        val userEntity = userRepository.findByIdAndDeletedAtIsNull(userId) ?: throw UserNotFoundException(mapOf("userId" to userId))
        userEntity.passwordHash = newPasswordHash
    }

    override fun existsUserByEmail(email: String): Boolean {
        return userRepository.existsByEmailAndDeletedAtIsNull(email)
    }

    override fun saveUser(user: User): User {
        return when (user) {
            is UserApplicant ->
                userRepository.save(
                    ApplicantEntity(
                        name = user.name,
                        email = user.email,
                        passwordHash = requireNotNull(user.passwordHash) { "Password hash must not be null" },
                    ),
                )
            is UserCompany ->
                userRepository.save(
                    CompanyEntity(
                        name = user.name,
                        email = user.email,
                        passwordHash = requireNotNull(user.passwordHash) { "Password hash must not be null" },
                        vcRec = user.vcRec,
                        vcName = user.vcName,
                    ),
                )
        }
            .toUser()
    }

    private fun UserEntity.toUser(): User {
        return when (this) {
            is ApplicantEntity ->
                UserApplicant(
                    id = requireNotNull(this.id) { "UserEntity id must not be null" },
                    name = this.name,
                    userRole = this.getRole(),
                    email = this.email.substringBeforeLast("#deleted"),
                    passwordHash = this.passwordHash,
                )
            is CompanyEntity ->
                UserCompany(
                    id = requireNotNull(this.id) { "UserEntity id must not be null" },
                    name = this.name,
                    userRole = this.getRole(),
                    email = this.email.substringBeforeLast("#deleted"),
                    passwordHash = this.passwordHash,
                    vcRec = this.vcRec,
                    vcName = this.vcName,
                )
            else -> throw IllegalStateException("Unknown user type ${this.javaClass.simpleName}")
        }
    }

    override fun deleteUser(userId: String) {
        val userEntity =
            userRepository.findByIdAndDeletedAtIsNull(userId)
                ?: throw IllegalArgumentException("User with id $userId not found")
        userEntity.deleteUser()
    }
}
