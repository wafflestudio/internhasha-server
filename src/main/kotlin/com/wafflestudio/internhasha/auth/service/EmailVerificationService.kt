package com.wafflestudio.internhasha.auth.service

import com.wafflestudio.internhasha.auth.exception.UserDuplicateSnuMailException
import com.wafflestudio.internhasha.auth.exception.UserEmailVerificationInvalidException
import com.wafflestudio.internhasha.auth.repository.AuthRedisRepository
import com.wafflestudio.internhasha.auth.repository.UserRepository
import com.wafflestudio.internhasha.auth.service.dto.CheckDuplicateMailCommand
import com.wafflestudio.internhasha.auth.service.dto.CheckSnuMailVerificationCommand
import com.wafflestudio.internhasha.auth.service.dto.SendSnuMailVerificationCommand
import com.wafflestudio.internhasha.common.event.EmailSendEvent
import com.wafflestudio.internhasha.email.domain.EmailType
import org.mindrot.jbcrypt.BCrypt
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class EmailVerificationService(
    private val userRepository: UserRepository,
    private val authRedisRepository: AuthRedisRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    // 메일(아이디) 중복 확인
    fun checkDuplicateMail(command: CheckDuplicateMailCommand) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(command.email)) {
            throw UserDuplicateSnuMailException(
                details = mapOf("email" to command.email),
            )
        }
    }

    // Email verification
    fun sendSnuMailVerification(command: SendSnuMailVerificationCommand) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(command.snuMail)) {
            throw UserDuplicateSnuMailException(
                details = mapOf("email" to command.snuMail),
            )
        }

        val emailCode = (100000..999999).random().toString()
        val encryptedEmailCode = BCrypt.hashpw(emailCode, BCrypt.gensalt())

        authRedisRepository.saveEmailCode(command.snuMail, encryptedEmailCode)
        applicationEventPublisher.publishEvent(
            EmailSendEvent(
                emailType = EmailType.VerifyMail,
                to = command.snuMail,
                subject = "[인턴하샤] 이메일 인증 안내",
                text = emailCode,
            ),
        )
    }

    fun checkSnuMailVerification(
        command: CheckSnuMailVerificationCommand,
    ): String {
        val result = authRedisRepository.verifyEmailCode(command.snuMail, command.code)
        if (!result) {
            throw UserEmailVerificationInvalidException(
                details = mapOf("snuMail" to command.snuMail),
            )
        } else {
            authRedisRepository.deleteEmailCode(command.snuMail)
            val successCode = UUID.randomUUID().toString()
            authRedisRepository.saveSuccessCode(command.snuMail, successCode)
            return successCode
        }
    }
}
