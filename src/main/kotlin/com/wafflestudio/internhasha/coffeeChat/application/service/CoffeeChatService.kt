package com.wafflestudio.internhasha.coffeeChat.application.service

import com.wafflestudio.internhasha.auth.domain.User
import com.wafflestudio.internhasha.auth.domain.UserRole
import com.wafflestudio.internhasha.coffeeChat.application.dto.CoffeeChatCreateCommand
import com.wafflestudio.internhasha.coffeeChat.application.port.CoffeeChatPersistencePort
import com.wafflestudio.internhasha.coffeeChat.domain.CoffeeChat
import com.wafflestudio.internhasha.coffeeChat.domain.CoffeeChatBrief
import com.wafflestudio.internhasha.coffeeChat.domain.CoffeeChatDuplicationException
import com.wafflestudio.internhasha.coffeeChat.domain.CoffeeChatNotFoundException
import com.wafflestudio.internhasha.coffeeChat.domain.CoffeeChatPostExpiredException
import com.wafflestudio.internhasha.coffeeChat.domain.CoffeeChatStatus
import com.wafflestudio.internhasha.coffeeChat.domain.CoffeeChatStatusForbiddenException
import com.wafflestudio.internhasha.coffeeChat.domain.CoffeeChatUserForbiddenException
import com.wafflestudio.internhasha.coffeeChat.web.controller.CoffeeChatContent
import com.wafflestudio.internhasha.coffeeChat.web.controller.CoffeeChatDetailList
import com.wafflestudio.internhasha.coffeeChat.web.controller.CoffeeChatStatusReq
import com.wafflestudio.internhasha.common.event.EmailSendEvent
import com.wafflestudio.internhasha.email.EmailType
import com.wafflestudio.internhasha.post.application.port.PositionPersistencePort
import com.wafflestudio.internhasha.post.domain.PostPositionNotFoundException
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class CoffeeChatService(
    private val coffeeChatPersistencePort: CoffeeChatPersistencePort,
    private val positionPersistencePort: PositionPersistencePort,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    @Value("\${custom.page.size:12}")
    private val pageSize: Int = 12

    @Transactional // 읽음 처리를 위한 트랜잭션
    fun getCoffeeChatDetail(
        user: User,
        coffeeChatId: String,
    ): CoffeeChat {
        val markAsRead = (user.userRole == UserRole.ROLE_APPLICANT)
        val coffeeChat =
            coffeeChatPersistencePort.findCoffeeChatById(coffeeChatId, markAsRead)
                ?: throw CoffeeChatNotFoundException()
        checkCoffeeChatAuthority(coffeeChat, user)
        return coffeeChat
    }

    fun checkIsSubmitted(
        applicant: User,
        positionId: String,
    ): Boolean {
        // 이미 대기 중인 커피챗 ID가 있으면 True
        return (
            coffeeChatPersistencePort.existsCoffeeChatByFilter(
                applicantId = applicant.id,
                positionId = positionId,
                status = CoffeeChatStatus.WAITING,
            )
        )
    }

    @Transactional
    fun applyCoffeeChat(
        applicant: User,
        positionId: String,
        coffeeChatContent: CoffeeChatContent,
    ): CoffeeChat {
        val isExists =
            coffeeChatPersistencePort.existsCoffeeChatByFilter(
                applicantId = applicant.id,
                positionId = positionId,
                status = CoffeeChatStatus.WAITING,
            )
        if (isExists) {
            throw CoffeeChatDuplicationException()
        }

        val position =
            positionPersistencePort.findPositionById(positionId)
                ?: throw PostPositionNotFoundException(
                    details = mapOf("positionId" to positionId),
                )

        // 지원 가능한지 마감시간을 확인
        if (position.employmentEndDate != null && LocalDateTime.now().isAfter(position.employmentEndDate!!)) {
            throw CoffeeChatPostExpiredException(
                details = mapOf("postId" to positionId, "endDate" to position.employmentEndDate.toString()),
            )
        }

        val coffeeChat =
            coffeeChatPersistencePort.createCoffeeChat(
                CoffeeChatCreateCommand(
                    applicantId = applicant.id,
                    positionId = positionId,
                    content = coffeeChatContent.content,
                ),
            )
        val waitingCoffeeChatCount =
            coffeeChatPersistencePort.countCoffeeChatByCompanyIdAndStatus(
                companyId = coffeeChat.company.id,
                status = CoffeeChatStatus.WAITING,
            )

        applicationEventPublisher.publishEvent(
            EmailSendEvent(
                to = requireNotNull(coffeeChat.company.email) { "Destination must not be null" },
                emailType = EmailType.Notification,
                subject = "[인턴하샤] 지원자 커피챗 안내",
                text = "",
                coffeeChat = coffeeChat,
                waitingCoffeeChatCount = waitingCoffeeChatCount,
            ),
        )

        return coffeeChat
    }

    @Transactional
    fun editCoffeeChat(
        applicant: User,
        coffeeChatId: String,
        coffeeChatContent: CoffeeChatContent,
    ): CoffeeChat {
        // 커피챗 찾기
        val coffeeChat =
            coffeeChatPersistencePort.findCoffeeChatById(coffeeChatId)
                ?: throw CoffeeChatNotFoundException()
        // 작성자가 아니면 403
        checkCoffeeChatAuthority(coffeeChat, applicant)

        // 대기 중인 커피챗만 수정 가능
        if (coffeeChat.coffeeChatStatus != CoffeeChatStatus.WAITING) {
            throw CoffeeChatStatusForbiddenException(
                details =
                    mapOf(
                        "coffeeChatId" to coffeeChatId,
                        "status" to coffeeChat.coffeeChatStatus.toString(),
                    ),
            )
        }

        // 업데이트
        return coffeeChatPersistencePort.updateCoffeeChatContent(
            coffeeChatId = coffeeChatId,
            content = coffeeChatContent.content,
        )
    }

    @Transactional
    fun changeCoffeeChatStatus(
        user: User,
        coffeeChatStatusReq: CoffeeChatStatusReq,
    ): CoffeeChatDetailList {
        // 하나라도 유효하지 않은 커피챗이 들어오면 404
        val coffeeChatList =
            coffeeChatStatusReq.coffeeChatList
                .map {
                    coffeeChatPersistencePort.findCoffeeChatById(it)
                        ?: throw CoffeeChatNotFoundException(
                            details = mapOf("coffeeChatId" to it),
                        )
                }

        val succeeded: MutableList<CoffeeChat> = mutableListOf()
        val failed: MutableList<CoffeeChat> = mutableListOf()

        // 변경 권한을 확인
        when (user.userRole) {
            UserRole.ROLE_APPLICANT -> {
                // 지원자는 취소만 가능
                if (coffeeChatStatusReq.coffeeChatStatus != CoffeeChatStatus.CANCELED) {
                    throw CoffeeChatUserForbiddenException(
                        details =
                            mapOf(
                                "userRole" to user.userRole,
                                "coffeeChatStatus" to coffeeChatStatusReq.coffeeChatStatus,
                            ),
                    )
                }
                // 본인이 작성한 커피챗만 & 대기 중인 커피챗만
                for (coffeeChat in coffeeChatList) {
                    if (coffeeChat.applicant.id != user.id ||
                        coffeeChat.coffeeChatStatus != CoffeeChatStatus.WAITING
                    ) {
                        failed.add(coffeeChat)
                    } else {
                        coffeeChatPersistencePort.updateCoffeeChatStatus(
                            coffeeChat.id,
                            coffeeChatStatusReq.coffeeChatStatus,
                        )
                        succeeded.add(coffeeChat)
                    }
                }
            }
            UserRole.ROLE_COMPANY -> {
                // 회사는 수락, 거절만 가능
                if (coffeeChatStatusReq.coffeeChatStatus != CoffeeChatStatus.ACCEPTED &&
                    coffeeChatStatusReq.coffeeChatStatus != CoffeeChatStatus.REJECTED
                ) {
                    throw CoffeeChatUserForbiddenException(
                        details =
                            mapOf(
                                "userRole" to user.userRole,
                                "coffeeChatStatus" to coffeeChatStatusReq.coffeeChatStatus,
                            ),
                    )
                }
                // 자신이 작성한 공고의 커피챗만 & 대기 중인 커피챗만
                for (coffeeChat in coffeeChatList) {
                    if (coffeeChat.company.id != user.id ||
                        coffeeChat.coffeeChatStatus != CoffeeChatStatus.WAITING
                    ) {
                        failed.add(coffeeChat)
                    } else {
                        coffeeChatPersistencePort.updateCoffeeChatStatus(
                            coffeeChat.id,
                            coffeeChatStatusReq.coffeeChatStatus,
                        )
                        succeeded.add(coffeeChat)
                        applicationEventPublisher.publishEvent(
                            EmailSendEvent(
                                emailType = EmailType.Result,
                                to = requireNotNull(coffeeChat.applicant.email) { "Destination must not be null" },
                                subject = "[인턴하샤] 커피챗 지원 결과 안내",
                                text = "",
                                coffeeChat = coffeeChat,
                            ),
                        )
                    }
                }
            }
        }
        return CoffeeChatDetailList(
            succeeded = succeeded,
            failed = failed,
        )
    }

    fun getCoffeeChatList(
        user: User,
    ): List<CoffeeChatBrief> {
        return when (user.userRole) {
            UserRole.ROLE_APPLICANT -> {
                coffeeChatPersistencePort.findAllCoffeeChatByApplicantId(user.id)
                    .map { CoffeeChatBrief.fromCoffeeChat(it) }
            }
            UserRole.ROLE_COMPANY -> {
                coffeeChatPersistencePort.findAllByCompanyIdExceptCanceled(user.id)
                    .map {
                        CoffeeChatBrief.fromCoffeeChat(it).run {
                            copy(applicant = applicant.copy(email = null))
                        }
                    }
            }
        }
    }

    fun countCoffeeChatBadges(
        user: User,
    ): Long {
        return when (user.userRole) {
            UserRole.ROLE_APPLICANT -> coffeeChatPersistencePort.countCoffeeChatByApplicantIdAndChangedTrue(applicantId = user.id)
            UserRole.ROLE_COMPANY ->
                coffeeChatPersistencePort.countCoffeeChatByCompanyIdAndStatus(
                    companyId = user.id,
                    status = CoffeeChatStatus.WAITING,
                )
        }
    }

    private fun checkCoffeeChatAuthority(
        coffeeChat: CoffeeChat,
        user: User,
    ) {
        when (user.userRole) {
            UserRole.ROLE_APPLICANT -> {
                if (coffeeChat.applicant.id != user.id) {
                    throw CoffeeChatUserForbiddenException(
                        details = mapOf("userId" to user.id, "userRole" to user.userRole),
                    )
                }
            }
            UserRole.ROLE_COMPANY -> {
                if (coffeeChat.company.id != user.id) {
                    throw CoffeeChatUserForbiddenException(
                        details = mapOf("userId" to user.id, "userRole" to user.userRole),
                    )
                }
            }
        }
    }

//    // 지원자 탈퇴 시 coffeeChat 데이터를 삭제
//    @Transactional
//    fun deleteCoffeeChatByUser(userEntity: UserEntity) {
//        coffeeChatRepository.deleteAllByApplicantId(userEntity.id)
//    }
}
