package com.waffletoy.team1server.user.service

import com.waffletoy.team1server.user.EmailServiceException
import com.waffletoy.team1server.user.persistence.UserRepository
import org.mindrot.jbcrypt.BCrypt
import org.springframework.http.HttpStatus
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.util.*

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val userRepository: UserRepository,
    private val redisTokenService: RedisTokenService,
) {
    @Async
    fun sendCode(
        snuMail: String,
    ) {
        // 이미 등록된 메일인지 확인
        if (userRepository.existsBySnuMail(snuMail)) {
            throw EmailServiceException(
                "동일한 스누메일로 등록된 계정이 존재합니다.",
                HttpStatus.CONFLICT,
            )
        }

        // 이메일 인증 토큰 생성
        val emailCode = (100000..999999).random().toString()

        val encryptedEmailCode = BCrypt.hashpw(emailCode, BCrypt.gensalt())

        // Redis 에 Email Token 저장
        redisTokenService.saveEmailCode(snuMail, encryptedEmailCode)

        // 이메일 발송
        try {
            val message = SimpleMailMessage()
            message.setTo(snuMail)
            message.subject = "이메일 인증 요청"
            message.text = "이메일 인증 번호: $emailCode"
            mailSender.send(message)
        } catch (ex: Exception) {
            throw EmailServiceException(
                "Sending Email Failure",
                HttpStatus.INTERNAL_SERVER_ERROR,
            )
        }
    }

    fun verifyToken(
        snuMail: String,
        code: String,
    ) {
        // Redis에서 암호화된 이메일 인증 코드 가져오기
        val encryptedCode =
            redisTokenService.getEmailCode(snuMail)
                ?: throw EmailServiceException(
                    "이메일로 전달된 코드가 유효하지 않습니다.",
                    HttpStatus.FORBIDDEN,
                )

        // 입력된 인증 코드와 Redis에 저장된 암호화된 코드 비교
        if (!BCrypt.checkpw(code, encryptedCode)) {
            throw EmailServiceException(
                "인증 코드와 입력 코드가 일치하지 않습니다.",
                HttpStatus.BAD_REQUEST,
            )
        } else {
            redisTokenService.deleteEmailCode(snuMail)
        }
    }
}
