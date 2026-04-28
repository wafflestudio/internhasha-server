package com.wafflestudio.internhasha.company.service

import com.wafflestudio.internhasha.common.domain.LinkVo
import com.wafflestudio.internhasha.common.domain.TagVo
import com.wafflestudio.internhasha.common.dto.S3Files
import com.wafflestudio.internhasha.common.event.S3FileCleanupEvent
import com.wafflestudio.internhasha.company.exception.CompanyNotFoundException
import com.wafflestudio.internhasha.company.repository.CompanyRepository
import com.wafflestudio.internhasha.company.service.dto.CompanyDto
import com.wafflestudio.internhasha.company.service.dto.CreateCompanyCommand
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CompanyService(
    private val companyRepository: CompanyRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    @Transactional
    fun putCompany(
        command: CreateCompanyCommand,
    ): CompanyDto {
        val company =
            companyRepository.findByIdAndDeletedAtIsNull(command.companyId)
                ?: throw CompanyNotFoundException()

        val filesToDelete =
            S3Files().apply {
                if (company.profileImageKey != command.profileImageKey) {
                    this.profileImageKey = company.profileImageKey
                }
                if (command.companyInfoPDFKey != null && company.companyInfoPDFKey != command.companyInfoPDFKey) {
                    this.companyInfoPDFKey = company.companyInfoPDFKey
                }
            }

        company.apply {
            this.companyEstablishedYear = command.companyEstablishedYear
            this.domain = command.domain
            this.headcount = command.headcount
            this.location = command.location
            this.slogan = command.slogan
            this.detail = command.detail
            this.profileImageKey = command.profileImageKey
            command.companyInfoPDFKey?.let { this.companyInfoPDFKey = it }
            command.landingPageLink?.let { this.landingPageLink = it }
            command.links?.let {
                this.links.clear()
                this.links.addAll(it.map { link -> LinkVo(link = link.link, description = link.description) })
            }
            command.tags?.let {
                this.tags.clear()
                this.tags.addAll(it.map { tag -> TagVo(tag = tag.tag) })
            }
        }

        deleteS3File(filesToDelete)

        return CompanyDto.fromEntity(company)
    }

    @Transactional
    fun getCompany(userId: String): CompanyDto {
        return companyRepository.findByIdAndDeletedAtIsNull(userId)?.let { CompanyDto.fromEntity(it) }
            ?: throw CompanyNotFoundException()
    }

    private fun deleteS3File(filesToDelete: S3Files) {
        if (filesToDelete.hasAnyFile()) {
            applicationEventPublisher.publishEvent(S3FileCleanupEvent(filesToDelete))
        }
    }
}
