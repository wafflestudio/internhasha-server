package com.waffletoy.team1server.user.service

import com.waffletoy.team1server.user.EmailServiceException
import com.waffletoy.team1server.user.persistence.UserEntity
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
    private val redisTokenService: RedisTokenService,
) {
    fun verifyToken(
        userId: String,
        token: String,
    ): String {
        // Redis에서 암호화된 이메일 인증 코드 가져오기
        val encryptedToken =
            redisTokenService.getEmailTokenByUserId(userId)
                ?: throw EmailServiceException(
                    "Invalid or expired email token",
                    HttpStatus.BAD_REQUEST,
                )

        // 입력된 인증 코드와 Redis에 저장된 암호화된 코드 비교
        return try {
            if (!BCrypt.checkpw(token, encryptedToken)) {
                throw EmailServiceException(
                    "Invalid email token",
                    HttpStatus.BAD_REQUEST,
                )
            }

            // 인증 성공 시 사용자 ID 반환
            userId
        } finally {
            // 토큰 삭제 (성공/실패 관계없이 무조건 삭제)
//            redisTokenService.deleteEmailTokenByUserId(userId)
        }
    }

    @Async
    fun sendEmailVerification(
        user: UserEntity,
        email: String,
    ) {
        // 이메일 인증 토큰 생성
        val emailCode = (100000..999999).random().toString()

        val encryptedEmailCode = BCrypt.hashpw(emailCode, BCrypt.gensalt())

        // Redis 에 Email Token 저장
        redisTokenService.saveEmailToken(user.id, encryptedEmailCode)

        // 이메일 발송
        try {
            val message = SimpleMailMessage()
            message.setTo(email)
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
}
