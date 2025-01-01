package com.waffletoy.team1server.user.service

import com.waffletoy.team1server.user.EmailTokenInvalidException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val redisTokenService: RedisTokenService,
) {
    fun sendEmail(
        to: String,
        subject: String,
        body: String,
    ) {
        val message = SimpleMailMessage()
        message.setTo(to)
        message.subject = subject
        message.text = body
        mailSender.send(message)
    }

    fun verifyToken(token: String): String {
        // Redis에서 이메일 토큰 확인
        val userId =
            redisTokenService.getUserIdByEmailToken(token)
                ?: throw EmailTokenInvalidException("Invalid or expired email token")

        // 토큰 삭제 (한 번 사용 후 무효화)
        redisTokenService.deleteEmailToken(userId)

        return userId
    }
}
