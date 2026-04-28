package com.wafflestudio.internhasha.applicant.service

import com.wafflestudio.internhasha.applicant.exception.ApplicantNotFoundException
import com.wafflestudio.internhasha.applicant.repository.ApplicantRepository
import com.wafflestudio.internhasha.applicant.service.dto.ApplicantDto
import com.wafflestudio.internhasha.applicant.service.dto.UpdateApplicantCommand
import com.wafflestudio.internhasha.auth.security.AuthUserPrincipal
import com.wafflestudio.internhasha.common.domain.LinkVo
import com.wafflestudio.internhasha.common.dto.S3Files
import com.wafflestudio.internhasha.common.event.S3FileCleanupEvent
import com.wafflestudio.internhasha.common.event.UserDeletedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ApplicantService(
    private val applicantRepository: ApplicantRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    fun getApplicant(
        authUser: AuthUserPrincipal,
    ): ApplicantDto {
        val applicant =
            applicantRepository.findApplicantByIdAndDeletedAtIsNull(authUser.id)
                ?: throw ApplicantNotFoundException()
        return ApplicantDto.fromEntity(applicant)
    }

    @Transactional
    fun putApplicant(
        command: UpdateApplicantCommand,
    ): ApplicantDto {
        val applicant =
            applicantRepository.findApplicantByIdAndDeletedAtIsNull(command.applicantId)
                ?: throw ApplicantNotFoundException()

        // 기존 s3 object 확인
        val filesToDelete =
            S3Files().apply {
                if (command.profileImageKey != null && applicant.profileImageKey != command.profileImageKey) {
                    this.profileImageKey = applicant.profileImageKey
                }
                if (command.cvKey != null && applicant.cvKey != command.cvKey) {
                    this.cvKey = applicant.cvKey
                }
                if (command.portfolioKey != null && applicant.portfolioKey != command.portfolioKey) {
                    this.portfolioKey = applicant.portfolioKey
                }
            }

        applicant.apply {
            enrollYear = command.enrollYear ?: this.enrollYear
            dept = command.department ?: this.dept
            slogan = command.slogan ?: this.slogan
            explanation = command.explanation ?: this.explanation
            profileImageKey = command.profileImageKey ?: this.profileImageKey
            cvKey = command.cvKey ?: this.cvKey
            portfolioKey = command.portfolioKey ?: this.portfolioKey
            if (command.positions != null) {
                this.positions.clear()
                this.positions.addAll(command.positions)
            }
            if (command.stacks != null) {
                this.stacks.clear()
                this.stacks.addAll(command.stacks)
            }
            if (command.links != null) {
                this.links.clear()
                this.links.addAll(command.links.map { LinkVo(it.description, it.link) })
            }
        }
        deleteS3File(filesToDelete)

        return ApplicantDto.fromEntity(applicant)
    }

    @EventListener
    fun handleUserDeletedEvent(event: UserDeletedEvent) {
        val applicant = applicantRepository.findByIdOrNull(event.userId) ?: return
        val filesToDelete =
            S3Files(
                cvKey = applicant.cvKey,
                profileImageKey = applicant.profileImageKey,
                portfolioKey = applicant.portfolioKey,
            )
        deleteS3File(filesToDelete)
    }

    private fun deleteS3File(filesToDelete: S3Files) {
        if (filesToDelete.hasAnyFile()) {
            applicationEventPublisher.publishEvent(S3FileCleanupEvent(filesToDelete))
        }
    }
}
