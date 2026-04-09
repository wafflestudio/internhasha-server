package com.wafflestudio.internhasha.applicant.application.port

import com.wafflestudio.internhasha.applicant.domain.Applicant

interface ApplicantPersistencePort {
    fun findApplicantById(applicantId: String): Applicant?

    fun findApplicantByIdIncludingDeleted(applicantId: String): Applicant?

    fun updateApplicant(applicant: Applicant): Applicant
}
