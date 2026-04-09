package com.wafflestudio.internhasha.company.application.service

import com.wafflestudio.internhasha.common.event.S3FileCleanupEvent
import com.wafflestudio.internhasha.company.application.dto.CreateCompanyCommand
import com.wafflestudio.internhasha.company.application.port.CompanyPersistencePort
import com.wafflestudio.internhasha.company.domain.Company
import com.wafflestudio.internhasha.company.domain.CompanyNotFoundException
import com.wafflestudio.internhasha.s3.dto.S3Files
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CompanyService(
    private val companyPersistencePort: CompanyPersistencePort,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    @Transactional
    fun putCompany(
        command: CreateCompanyCommand,
    ): Company {
        val company =
            companyPersistencePort.findCompanyByCompanyId(command.companyId)
                ?: throw CompanyNotFoundException()

        val filesToDelete =
            S3Files().apply {
                if (command.profileImageKey != null && company.profileImageKey != command.profileImageKey) {
                    this.profileImageKey = company.profileImageKey
                }
                if (command.companyInfoPDFKey != null && company.companyInfoPDFKey != command.companyInfoPDFKey) {
                    this.companyInfoPDFKey = company.companyInfoPDFKey
                }
            }

        val updatedCompany =
            companyPersistencePort.updateCompany(
                command.toUpdatedDomain(company),
            )
        deleteS3File(filesToDelete)

        return updatedCompany
    }

    @Transactional
    fun getCompany(userId: String): Company {
        return companyPersistencePort.findCompanyByCompanyId(userId)
            ?: throw CompanyNotFoundException()
    }

    private fun deleteS3File(filesToDelete: S3Files) {
        if (filesToDelete.hasAnyFile()) {
            applicationEventPublisher.publishEvent(S3FileCleanupEvent(filesToDelete))
        }
    }
}
