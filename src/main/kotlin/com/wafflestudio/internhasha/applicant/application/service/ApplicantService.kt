package com.wafflestudio.internhasha.applicant.application.service

import com.wafflestudio.internhasha.applicant.ApplicantNotFoundException
import com.wafflestudio.internhasha.applicant.application.dto.UpdateApplicantCommand
import com.wafflestudio.internhasha.applicant.application.port.ApplicantPersistencePort
import com.wafflestudio.internhasha.applicant.domain.Applicant
import com.wafflestudio.internhasha.auth.domain.User
import com.wafflestudio.internhasha.common.event.S3FileCleanupEvent
import com.wafflestudio.internhasha.common.event.UserDeletedEvent
import com.wafflestudio.internhasha.s3.dto.S3Files
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ApplicantService(
    private val applicantPersistencePort: ApplicantPersistencePort,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    fun getApplicant(
        user: User,
    ): Applicant {
        val applicant =
            applicantPersistencePort.findApplicantById(user.id)
                ?: throw ApplicantNotFoundException()
        return applicant
    }

    @Transactional
    fun putApplicant(
        command: UpdateApplicantCommand,
    ): Applicant {
        val applicant =
            applicantPersistencePort.findApplicantById(command.applicantId)
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

        val updatedApplicant =
            applicantPersistencePort.updateApplicant(
                command.toUpdatedDomain(applicant),
            )
        deleteS3File(filesToDelete)

        return updatedApplicant
    }

    @EventListener
    fun handleUserDeletedEvent(event: UserDeletedEvent) {
        val applicant = applicantPersistencePort.findApplicantByIdIncludingDeleted(event.userId) ?: return
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
