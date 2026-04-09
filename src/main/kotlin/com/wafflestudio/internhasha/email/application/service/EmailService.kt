package com.wafflestudio.internhasha.email.application.service

import com.wafflestudio.internhasha.coffeeChat.domain.CoffeeChat
import com.wafflestudio.internhasha.email.EmailSendFailureException
import com.wafflestudio.internhasha.email.EmailType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.SpringTemplateEngine

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val templateEngine: SpringTemplateEngine,
    private val applicationScope: CoroutineScope,
    @Value("\${custom.domain-name}") private val domainName: String,
    @Value("\${custom.protocol}") private val protocol: String,
) {
    private val logger: Logger = LoggerFactory.getLogger(EmailService::class.java)

    fun sendEmailAsync(
        type: EmailType,
        to: String,
        subject: String,
        text: String,
        coffeeChat: CoffeeChat? = null,
        waitingCoffeeChatCount: Long = 0,
    ) {
        applicationScope.launch {
            sendEmail(type, to, subject, text, coffeeChat, waitingCoffeeChatCount)
        }
    }

    suspend fun sendEmail(
        type: EmailType,
        to: String,
        subject: String,
        text: String,
        coffeeChat: CoffeeChat? = null,
        waitingCoffeeChatCount: Long = 0,
    ) = withContext(Dispatchers.IO) {
        try {
            val message = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")
            val context = Context()

            val templateName: String

            var modifiedContent: String?
            if (coffeeChat != null) {
                val content = coffeeChat.content
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
                    if (coffeeChat != null) {
                        context.setVariable("title", coffeeChat.positionTitle)
                        context.setVariable("name", coffeeChat.applicant.name)
                        context.setVariable("email", coffeeChat.applicant.email)
                        context.setVariable("content", modifiedContent)
                        context.setVariable("coffeeChatId", coffeeChat.id)
                        context.setVariable("waiting", waitingCoffeeChatCount.toString())
                    }

                    templateName = "coffeeChatNotification"
                }
                EmailType.Result -> {
                    context.setVariable("domain", "$protocol://$domainName")
                    if (coffeeChat != null) {
                        context.setVariable("title", coffeeChat.positionTitle)
                        context.setVariable("content", modifiedContent)
                        context.setVariable("coffeeChatId", coffeeChat.id)
                        context.setVariable("status", coffeeChat.coffeeChatStatus.toString())
                        context.setVariable("companyName", coffeeChat.company.name)
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
            logger.error("이메일 전송 실패: 대상 = $to, 타입 = $type", ex)
            throw EmailSendFailureException()
        }
    }
}
