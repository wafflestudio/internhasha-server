package com.waffletoy.team1server.user.service

import com.waffletoy.team1server.user.EmailServiceException
import com.waffletoy.team1server.user.persistence.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val userRepository: UserRepository,
    private val userRedisCacheService: UserRedisCacheService,
) {
    fun sendEmail(
        to: String,
        subject: String,
        text: String,
    ) {
        try {
            val message = SimpleMailMessage()
            message.setTo(to)
            message.setSubject(subject)
            message.setText(text)
            mailSender.send(message)
        } catch (ex: Exception) {
            throw EmailServiceException(
                "Sending Email Failure",
                HttpStatus.INTERNAL_SERVER_ERROR,
            )
        }
    }
}
