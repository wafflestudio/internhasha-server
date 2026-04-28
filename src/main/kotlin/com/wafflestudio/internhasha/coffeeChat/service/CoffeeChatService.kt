package com.wafflestudio.internhasha.coffeeChat.service

import com.wafflestudio.internhasha.applicant.repository.ApplicantRepository
import com.wafflestudio.internhasha.auth.domain.UserRole
import com.wafflestudio.internhasha.auth.security.AuthUserPrincipal
import com.wafflestudio.internhasha.coffeeChat.controller.dto.request.CoffeeChatContent
import com.wafflestudio.internhasha.coffeeChat.controller.dto.request.CoffeeChatStatusReq
import com.wafflestudio.internhasha.coffeeChat.domain.CoffeeChat
import com.wafflestudio.internhasha.coffeeChat.domain.CoffeeChatStatus
import com.wafflestudio.internhasha.coffeeChat.exception.CoffeeChatDuplicationException
import com.wafflestudio.internhasha.coffeeChat.exception.CoffeeChatNotFoundException
import com.wafflestudio.internhasha.coffeeChat.exception.CoffeeChatPostExpiredException
import com.wafflestudio.internhasha.coffeeChat.exception.CoffeeChatStatusForbiddenException
import com.wafflestudio.internhasha.coffeeChat.exception.CoffeeChatUserForbiddenException
import com.wafflestudio.internhasha.coffeeChat.persistence.CoffeeChatRepository
import com.wafflestudio.internhasha.coffeeChat.service.dto.CoffeeChatBrief
import com.wafflestudio.internhasha.coffeeChat.service.dto.CoffeeChatCreateCommand
import com.wafflestudio.internhasha.coffeeChat.service.dto.CoffeeChatDetailList
import com.wafflestudio.internhasha.coffeeChat.service.dto.CoffeeChatDto
import com.wafflestudio.internhasha.common.event.EmailSendEvent
import com.wafflestudio.internhasha.email.domain.EmailType
import com.wafflestudio.internhasha.post.exception.PostPositionNotFoundException
import com.wafflestudio.internhasha.post.repository.PositionRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class CoffeeChatService(
    private val applicantRepository: ApplicantRepository,
    private val coffeeChatRepository: CoffeeChatRepository,
    private val positionRepository: PositionRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    @param:Value("\${custom.page.size:12}")
    private val pageSize: Int = 12,
) {
    @Transactional // 읽음 처리를 위한 트랜잭션
    fun getCoffeeChatDetail(
        user: AuthUserPrincipal,
        coffeeChatId: String,
    ): CoffeeChatDto {
        val coffeeChat =
            coffeeChatRepository.findByIdOrNull(coffeeChatId)
                ?: throw CoffeeChatNotFoundException()
        if (user.userRole == UserRole.ROLE_APPLICANT) coffeeChat.markAsRead()

        checkCoffeeChatAuthority(coffeeChat, user)
        return CoffeeChatDto.fromEntity(coffeeChat)
    }

    fun checkIsSubmitted(
        applicant: AuthUserPrincipal,
        positionId: String,
    ): Boolean {
        // 이미 대기 중인 커피챗 ID가 있으면 True
        return (
            coffeeChatRepository.existsByApplicantIdAndPositionIdAndCoffeeChatStatus(
                applicantId = applicant.id,
                positionId = positionId,
                status = CoffeeChatStatus.WAITING,
            )
        )
    }

    @Transactional
    fun applyCoffeeChat(
        command: CoffeeChatCreateCommand,
    ): CoffeeChatDto {
        val isExists =
            coffeeChatRepository.existsByApplicantIdAndPositionIdAndCoffeeChatStatus(
                applicantId = command.applicantId,
                positionId = command.positionId,
                status = CoffeeChatStatus.WAITING,
            )
        if (isExists) {
            throw CoffeeChatDuplicationException()
        }

        val position =
            positionRepository.findByIdOrNullFetchCompany(command.positionId)
                ?: throw PostPositionNotFoundException(
                    details = mapOf("positionId" to command.positionId),
                )

        // 지원 가능한지 마감시간을 확인
        if (position.employmentEndDate != null && LocalDateTime.now().isAfter(position.employmentEndDate!!)) {
            throw CoffeeChatPostExpiredException(
                details = mapOf("postId" to command.positionId, "endDate" to position.employmentEndDate.toString()),
            )
        }

        val coffeeChat =
            coffeeChatRepository.save(
                CoffeeChat(
                    content = command.content,
                    applicant = applicantRepository.getReferenceById(command.applicantId),
                    position = positionRepository.getReferenceById(command.positionId),
                ),
            )
        val waitingCoffeeChatCount =
            coffeeChatRepository.countByCompanyIdAndStatus(
                companyId = position.company.requireId(),
                status = CoffeeChatStatus.WAITING,
            )

        applicationEventPublisher.publishEvent(
            EmailSendEvent(
                to = requireNotNull(position.company.email) { "Destination must not be null" },
                emailType = EmailType.Notification,
                subject = "[인턴하샤] 지원자 커피챗 안내",
                text = "",
                coffeeChat = CoffeeChatDto.fromEntity(coffeeChat),
                waitingCoffeeChatCount = waitingCoffeeChatCount,
            ),
        )

        return CoffeeChatDto.fromEntity(coffeeChat)
    }

    @Transactional
    fun editCoffeeChat(
        applicant: AuthUserPrincipal,
        coffeeChatId: String,
        coffeeChatContent: CoffeeChatContent,
    ): CoffeeChatDto {
        // 커피챗 찾기
        val coffeeChat =
            coffeeChatRepository.findByIdOrNull(coffeeChatId)
                ?: throw CoffeeChatNotFoundException()
        if (applicant.userRole == UserRole.ROLE_APPLICANT) coffeeChat.markAsRead()
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
        coffeeChat.content = coffeeChatContent.content
        return CoffeeChatDto.fromEntity(coffeeChat)
    }

    @Transactional
    fun changeCoffeeChatStatus(
        user: AuthUserPrincipal,
        coffeeChatStatusReq: CoffeeChatStatusReq,
    ): CoffeeChatDetailList {
        // 하나라도 유효하지 않은 커피챗이 들어오면 404
        val coffeeChatList =
            coffeeChatStatusReq.coffeeChatList
                .map {
                    coffeeChatRepository.findByIdOrNull(it)
                        ?: throw CoffeeChatNotFoundException(
                            details = mapOf("coffeeChatId" to it),
                        )
                }

        val succeeded: MutableList<CoffeeChatDto> = mutableListOf()
        val failed: MutableList<CoffeeChatDto> = mutableListOf()

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
                        failed.add(CoffeeChatDto.fromEntity(coffeeChat))
                    } else {
                        coffeeChat.updateStatus(coffeeChatStatusReq.coffeeChatStatus)
                        succeeded.add(CoffeeChatDto.fromEntity(coffeeChat))
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
                    if (coffeeChat.position.company.id != user.id ||
                        coffeeChat.coffeeChatStatus != CoffeeChatStatus.WAITING
                    ) {
                        failed.add(CoffeeChatDto.fromEntity(coffeeChat))
                    } else {
                        coffeeChat.updateStatus(coffeeChatStatusReq.coffeeChatStatus)
                        succeeded.add(CoffeeChatDto.fromEntity(coffeeChat))
                        applicationEventPublisher.publishEvent(
                            EmailSendEvent(
                                emailType = EmailType.Result,
                                to = requireNotNull(coffeeChat.applicant.email) { "Destination must not be null" },
                                subject = "[인턴하샤] 커피챗 지원 결과 안내",
                                text = "",
                                coffeeChat = CoffeeChatDto.fromEntity(coffeeChat),
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
        user: AuthUserPrincipal,
    ): List<CoffeeChatBrief> =
        when (user.userRole) {
            UserRole.ROLE_APPLICANT -> {
                coffeeChatRepository
                    .findAllByApplicantId(user.id)
                    .map { CoffeeChatBrief.fromDto(CoffeeChatDto.fromEntity(it)) }
            }
            UserRole.ROLE_COMPANY -> {
                coffeeChatRepository
                    .findAllExceptStatusByCompanyId(
                        companyId = user.id,
                        excludedStatus = CoffeeChatStatus.CANCELED,
                    ).map {
                        // 이메일 익명화 처리
                        CoffeeChatBrief.fromDto(CoffeeChatDto.fromEntity(it)).run {
                            copy(applicant = applicant.copy(email = null))
                        }
                    }
            }
        }

    fun countCoffeeChatBadges(
        user: AuthUserPrincipal,
    ): Long =
        when (user.userRole) {
            UserRole.ROLE_APPLICANT -> coffeeChatRepository.countByApplicantIdAndChangedTrue(applicantId = user.id)
            UserRole.ROLE_COMPANY ->
                coffeeChatRepository.countByCompanyIdAndStatus(
                    companyId = user.id,
                    status = CoffeeChatStatus.WAITING,
                )
        }

    private fun checkCoffeeChatAuthority(
        coffeeChat: CoffeeChat,
        user: AuthUserPrincipal,
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
                if (coffeeChat.position.company.id != user.id) {
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
