package com.wafflestudio.internhasha.auth.service

import com.wafflestudio.internhasha.auth.exception.UserNotFoundException
import com.wafflestudio.internhasha.auth.repository.UserRepository
import com.wafflestudio.internhasha.auth.security.AuthUserPrincipal
import com.wafflestudio.internhasha.auth.security.PasswordGenerator
import com.wafflestudio.internhasha.auth.service.dto.ChangePasswordCommand
import com.wafflestudio.internhasha.auth.service.dto.ResetPasswordCommand
import com.wafflestudio.internhasha.common.event.EmailSendEvent
import com.wafflestudio.internhasha.common.event.UserDeletedEvent
import com.wafflestudio.internhasha.email.domain.EmailType
import com.wafflestudio.internhasha.exceptions.InvalidCredentialsException
import org.mindrot.jbcrypt.BCrypt
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AccountService(
    private val userRepository: UserRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    @Transactional
    fun changePassword(command: ChangePasswordCommand) {
        val user =
            userRepository.findByIdAndDeletedAtIsNull(command.userId)
                ?: throw UserNotFoundException(
                    details = mapOf("userId" to command.userId),
                )

        if (!BCrypt.checkpw(command.oldPassword, user.passwordHash)) {
            throw InvalidCredentialsException(
                details = mapOf("oldPassword" to command.oldPassword),
            )
        }

        // 새 비밀번호로 갱신
        val newPasswordHash = BCrypt.hashpw(command.newPassword, BCrypt.gensalt())
        user.passwordHash = newPasswordHash
    }

    @Transactional
    fun resetPassword(command: ResetPasswordCommand) {
        // 메일을 기준으로 유저 찾기
        val user =
            userRepository.findByEmailAndDeletedAtIsNull(command.email)
                ?: throw UserNotFoundException(
                    details = mapOf("email" to command.email),
                )

        val newPassword = PasswordGenerator.generateRandomPassword()
        val newPasswordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt())
        user.passwordHash = newPasswordHash

        // 이메일 발송은 비동기로 진행
        applicationEventPublisher.publishEvent(
            EmailSendEvent(
                emailType = EmailType.ResetPassword,
                to = user.email,
                subject = "[인턴하샤] 임시 비밀번호 발급 안내",
                text = newPassword,
            ),
        )
    }

    @Transactional
    fun withdrawUser(authUser: AuthUserPrincipal) {
        // SoftDelete 적용
        val user =
            userRepository.findByIdAndDeletedAtIsNull(authUser.id)
                ?: throw UserNotFoundException(details = mapOf("userId" to authUser.id))
        user.deleteUser()
        applicationEventPublisher.publishEvent(UserDeletedEvent(user.requireId(), user.getRole()))
        // 커피챗 쪽은 soft delete 필요
    }
}
