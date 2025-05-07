package com.wafflestudio.internhasha.email.service

import com.wafflestudio.internhasha.auth.dto.User
import com.wafflestudio.internhasha.coffeeChat.persistence.CoffeeChatEntity
import com.wafflestudio.internhasha.coffeeChat.service.CoffeeChatService
import com.wafflestudio.internhasha.email.EmailSendFailureException
import com.wafflestudio.internhasha.email.EmailType
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.SpringTemplateEngine

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val templateEngine: SpringTemplateEngine,
    @Lazy private val coffeeChatService: CoffeeChatService,
    @Value("\${custom.domain-name}") private val domainName: String,
    @Value("\${custom.protocol}") private val protocol: String,
) {
    @Async
    fun sendEmail(
        type: EmailType,
        to: String,
        subject: String,
        text: String,
        coffeeChatEntity: CoffeeChatEntity? = null,
    ) {
        try {
            val message = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")
            val context = Context()

            val templateName: String

            var modifiedContent: String?
            if (coffeeChatEntity != null) {
                val content = coffeeChatEntity.content
                modifiedContent =
                    (
                        if (content.length > 500) {
                            content.substring(0, 500) + "......<br/><br/><strong>[전체 내용은 마이페이지에서 확인해주세요]</strong>"
                        } else {
                            content
                        }
                    ).replace("\n", "<br/>")
            } else {
                modifiedContent = ""
            }

            when (type) {
                EmailType.VerifyMail -> {
                    context.setVariable("emailCode", text)
                    context.setVariable("email", to)
                    templateName = "verifyEmail"
                }
                EmailType.ResetPassword -> {
                    context.setVariable("newPassword", text)
                    context.setVariable("email", to)
                    templateName = "resetPassword"
                }
                EmailType.Notification -> {
                    context.setVariable("domain", "$protocol://$domainName")
                    if (coffeeChatEntity != null) {
                        context.setVariable("title", coffeeChatEntity.position.positionTitle)
                        context.setVariable("name", coffeeChatEntity.applicant.name)
                        context.setVariable("email", coffeeChatEntity.applicant.email)
                        context.setVariable("content", modifiedContent)
                        context.setVariable("coffeeChatId", coffeeChatEntity.id)
                        context.setVariable("waiting", coffeeChatService.countCoffeeChatBadges(User.fromEntity(coffeeChatEntity.position.company.user)).toString())
                    }

                    templateName = "coffeeChatNotification"
                }
                EmailType.Result -> {
                    context.setVariable("domain", "$protocol://$domainName")
                    if (coffeeChatEntity != null) {
                        context.setVariable("title", coffeeChatEntity.position.positionTitle)
                        context.setVariable("content", modifiedContent)
                        context.setVariable("coffeeChatId", coffeeChatEntity.id)
                        context.setVariable("status", coffeeChatEntity.coffeeChatStatus.toString())
                        context.setVariable("companyName", coffeeChatEntity.position.company.user.name)
                    }

                    templateName = "coffeeChatResult"
                }
            }

            // Thymeleaf 템플릿
            val content: String = templateEngine.process(templateName, context)

            // 이메일 정보 설정
            helper.setTo(to)
            helper.setSubject(subject)
            helper.setText(content, true)

            mailSender.send(message)
        } catch (ex: Exception) {
            throw EmailSendFailureException()
        }
    }
}
