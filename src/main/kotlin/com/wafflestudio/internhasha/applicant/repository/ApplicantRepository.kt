package com.wafflestudio.internhasha.applicant.repository

import com.wafflestudio.internhasha.applicant.domain.Applicant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ApplicantRepository : JpaRepository<Applicant, String> {
    @Query(
        """
        select distinct a
        from Applicant a
        left join fetch a.positions
        left join fetch a.stacks
        left join fetch a.links
        where a.id = :applicantId
          and a.deletedAt is null
    """,
    )
    fun findApplicantByIdAndDeletedAtIsNull(applicantId: String): Applicant?
}
