package com.waffletoy.team1server.email.service

import com.waffletoy.team1server.exceptions.EmailVerificationSendFailureException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender,
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
            throw EmailVerificationSendFailureException()
        }
    }
}
