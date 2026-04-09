package com.wafflestudio.internhasha.applicant.persistence

import com.wafflestudio.internhasha.applicant.ApplicantNotFoundException
import com.wafflestudio.internhasha.applicant.application.port.ApplicantPersistencePort
import com.wafflestudio.internhasha.applicant.domain.Applicant
import com.wafflestudio.internhasha.common.domain.Link
import com.wafflestudio.internhasha.common.persistence.LinkVo
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class ApplicantPersistenceAdapter(
    private val applicantRepository: ApplicantRepository,
) : ApplicantPersistencePort {
    override fun findApplicantById(applicantId: String): Applicant? {
        return applicantRepository.findByIdAndDeletedAtIsNull(applicantId)?.toApplicant()
    }

    override fun findApplicantByIdIncludingDeleted(applicantId: String): Applicant? {
        return applicantRepository.findByIdOrNull(applicantId)?.toApplicant()
    }

    @Transactional
    override fun updateApplicant(applicant: Applicant): Applicant {
        val applicantEntity =
            applicantRepository.findByIdAndDeletedAtIsNull(applicant.id)
                ?: throw ApplicantNotFoundException()

        return applicantEntity.apply {
            enrollYear?.let { this.enrollYear = it }
            dept?.let { this.dept = it }
            if (positions != null) {
                this.positions.clear()
                this.positions.addAll(positions)
            }
            slogan?.let { this.slogan = slogan }
            explanation?.let { this.explanation = explanation }
            if (stacks != null) {
                this.stacks.clear()
                this.stacks.addAll(stacks)
            }
            profileImageKey?.let { this.profileImageKey = profileImageKey }
            cvKey?.let { this.cvKey = cvKey }
            portfolioKey?.let { this.portfolioKey = portfolioKey }
            if (links != null) {
                this.links.clear()
                this.links.addAll(links.map { LinkVo(it.description, it.link) })
            }
        }.toApplicant()
    }

    private fun ApplicantEntity.toApplicant() =
        Applicant(
            id = requireNotNull(this.id) { "ApplicantEntity id must not be null" },
            name = this.name,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            userRole = this.getRole(),
            email = this.email,
            enrollYear = this.enrollYear,
            department = this.dept,
            positions = this.positions.toList(),
            slogan = this.slogan,
            explanation = this.explanation,
            stacks = this.stacks.toList(),
            profileImageKey = this.profileImageKey,
            cvKey = this.cvKey,
            portfolioKey = this.portfolioKey,
            links = this.links.map { Link(link = it.link, description = it.description) },
        )
}
