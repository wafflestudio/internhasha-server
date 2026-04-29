package com.wafflestudio.internhasha.email.service

import com.wafflestudio.internhasha.common.event.EmailSendEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class EmailSendHandler(
    private val emailService: EmailService,
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleEmailSend(event: EmailSendEvent) {
        emailService.sendEmailAsync(
            type = event.emailType,
            to = event.to,
            subject = event.subject,
            text = event.text,
            coffeeChat = event.coffeeChat,
            waitingCoffeeChatCount = event.waitingCoffeeChatCount,
        )
    }
}
