package com.wafflestudio.internhasha.coffeeChat.persistence

import com.wafflestudio.internhasha.applicant.persistence.ApplicantEntity
import com.wafflestudio.internhasha.applicant.persistence.ApplicantRepository
import com.wafflestudio.internhasha.auth.persistence.UserEntity
import com.wafflestudio.internhasha.coffeeChat.application.dto.CoffeeChatCreateCommand
import com.wafflestudio.internhasha.coffeeChat.application.port.CoffeeChatPersistencePort
import com.wafflestudio.internhasha.coffeeChat.domain.CoffeeChat
import com.wafflestudio.internhasha.coffeeChat.domain.CoffeeChatNotFoundException
import com.wafflestudio.internhasha.coffeeChat.domain.CoffeeChatStatus
import com.wafflestudio.internhasha.coffeeChat.domain.CoffeeChatUserInfo
import com.wafflestudio.internhasha.company.persistence.CompanyEntity
import com.wafflestudio.internhasha.post.persistence.PositionRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class CoffeeChatPersistenceAdapter(
    private val coffeeChatRepository: CoffeeChatRepository,
    private val applicantRepository: ApplicantRepository,
    private val positionRepository: PositionRepository,
) : CoffeeChatPersistencePort {
    override fun findCoffeeChatById(
        coffeeChatId: String,
        markAsRead: Boolean,
    ): CoffeeChat? {
        val entity = coffeeChatRepository.findByIdOrNull(coffeeChatId)
        if (markAsRead) entity?.markAsRead()
        return entity?.toCoffeeChat()
    }

    override fun findCoffeeChatByFilter(
        applicantId: String,
        positionId: String,
        status: CoffeeChatStatus,
    ): CoffeeChat? {
        return coffeeChatRepository.findCoffeeChatByFilter(
            applicantId = applicantId,
            positionId = positionId,
            status = status,
        )
            ?.toCoffeeChat()
    }

    override fun existsCoffeeChatByFilter(
        applicantId: String,
        positionId: String,
        status: CoffeeChatStatus,
    ): Boolean {
        return coffeeChatRepository.existsByApplicantIdAndPositionIdAndCoffeeChatStatus(
            applicantId = applicantId,
            positionId = positionId,
            status = status,
        )
    }

    override fun findAllCoffeeChatByApplicantId(applicantId: String): List<CoffeeChat> {
        return coffeeChatRepository.findAllByApplicantId(applicantId)
            .map { it.toCoffeeChat() }
    }

    override fun findAllByCompanyIdExceptCanceled(companyId: String): List<CoffeeChat> {
        return coffeeChatRepository.findAllExceptStatusByCompanyId(
            companyId = companyId,
            excludedStatus = CoffeeChatStatus.CANCELED,
        )
            .map { it.toCoffeeChat() }
    }

    override fun countCoffeeChatByApplicantIdAndChangedTrue(applicantId: String): Long {
        return coffeeChatRepository.countByApplicantIdAndChangedTrue(applicantId)
    }

    override fun countCoffeeChatByCompanyIdAndStatus(
        companyId: String,
        status: CoffeeChatStatus,
    ): Long {
        return coffeeChatRepository.countByCompanyIdAndStatus(companyId = companyId, status = status)
    }

    @Transactional
    override fun createCoffeeChat(command: CoffeeChatCreateCommand): CoffeeChat {
        val entity =
            CoffeeChatEntity(
                content = command.content,
                applicantEntity = applicantRepository.getReferenceById(command.applicantId),
                positionEntity = positionRepository.getReferenceById(command.positionId),
            )
        return coffeeChatRepository.save(entity).toCoffeeChat()
    }

    @Transactional
    override fun updateCoffeeChatContent(
        coffeeChatId: String,
        content: String,
    ): CoffeeChat {
        val entity = coffeeChatRepository.findByIdOrNull(coffeeChatId) ?: throw CoffeeChatNotFoundException()
        entity.content = content
        return entity.toCoffeeChat()
    }

    override fun updateCoffeeChatStatus(
        coffeeChatId: String,
        status: CoffeeChatStatus,
    ): CoffeeChat {
        val entity = coffeeChatRepository.findByIdOrNull(coffeeChatId) ?: throw CoffeeChatNotFoundException()
        entity.updateStatus(status)
        return entity.toCoffeeChat()
    }

    private fun CoffeeChatEntity.toCoffeeChat(): CoffeeChat {
        return CoffeeChat(
            id = requireNotNull(this.id) { "CoffeeChatEntity id must not be null" },
            positionId = requireNotNull(this.position.id) { "PositionEntity id must not be null" },
            positionTitle = this.position.positionTitle,
            positionCategory = this.position.positionType,
            company = this.position.company.let { getCoffeeChatUserInfoFromUserEntity(it) },
            createdAt = requireNotNull(this.createdAt) { "Timestamp must not be null" },
            updatedAt = requireNotNull(this.updatedAt) { "Timestamp must not be null" },
            coffeeChatStatus = this.coffeeChatStatus,
            changed = this.changed,
            content = this.content,
            applicant = this.applicant.let { getCoffeeChatUserInfoFromUserEntity(it) },
        )
    }

    private fun getCoffeeChatUserInfoFromUserEntity(entity: UserEntity): CoffeeChatUserInfo {
        return when (entity) {
            is ApplicantEntity ->
                CoffeeChatUserInfo(
                    id = requireNotNull(entity.id) { "Id must not be null" },
                    name = entity.name,
                    email = entity.email,
                    profileImageKey = entity.profileImageKey,
                )
            is CompanyEntity ->
                CoffeeChatUserInfo(
                    id = requireNotNull(entity.id) { "Id must not be null" },
                    name = entity.name,
                    email = entity.email,
                    profileImageKey = entity.profileImageKey,
                )
            else -> throw IllegalStateException("Unknown user type ${entity.javaClass.simpleName}")
        }
    }

    fun deleteAllByUserId(userId: String) {
        coffeeChatRepository.deleteAllByApplicantId(userId)
    }
}
