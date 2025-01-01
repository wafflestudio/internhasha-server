package com.waffletoy.team1server.user.service

import com.waffletoy.team1server.user.EmailSendException
import com.waffletoy.team1server.user.EmailTokenInvalidException
import com.waffletoy.team1server.user.persistence.UserEntity
import org.springframework.beans.factory.annotation.Value
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
    fun verifyToken(token: String): String {
        // Redis에서 이메일 토큰 확인
        val userId =
            redisTokenService.getUserIdByEmailToken(token)
                ?: throw EmailTokenInvalidException("Invalid or expired email token")

        // 토큰 삭제 (한 번 사용 후 무효화)
        redisTokenService.deleteEmailTokenByUserId(userId)

        return userId
    }

    @Async
    fun sendEmailVerification(
        user: UserEntity,
        email: String,
    ) {
        // 이메일 인증 토큰 생성
        val emailToken = UUID.randomUUID().toString()

        // Redis 에 Email Token 저장
        redisTokenService.saveEmailToken(user.id, emailToken)

        // 이메일 인증 링크 생성
        val verifyLink = "https://$domainUrl/api/verify-email?token=$emailToken"
        // 이메일 발송
        try {
            val message = SimpleMailMessage()
            message.setTo(email)
            message.subject = "이메일 인증 요청"
            message.text = "이메일 인증 링크: $verifyLink"
            mailSender.send(message)
        } catch (ex: Exception) {
            throw EmailSendException()
        }
    }

    @Value("\${custom.domain-url}")
    private lateinit var domainUrl: String
}
