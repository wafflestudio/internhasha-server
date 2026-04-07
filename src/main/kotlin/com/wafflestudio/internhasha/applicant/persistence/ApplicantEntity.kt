package com.wafflestudio.internhasha.applicant.persistence

import com.wafflestudio.internhasha.auth.UserRole
import com.wafflestudio.internhasha.auth.persistence.UserEntity
import com.wafflestudio.internhasha.common.LinkVo
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table

@Entity
@Table(name = "applicants")
@DiscriminatorValue(value = "applicant")
class ApplicantEntity(
    name: String,
    email: String,
    passwordHash: String? = null,
    var enrollYear: Int? = null,
    var dept: String? = null,
    var slogan: String? = null,
    @Column(columnDefinition = "TEXT")
    var explanation: String? = null,
    var profileImageKey: String? = null,
    var cvKey: String? = null,
    var portfolioKey: String? = null,
) : UserEntity(
        name = name,
        email = email,
        passwordHash = passwordHash,
    ) {
    @ElementCollection
    @CollectionTable(
        name = "applicant_positions",
        joinColumns = [JoinColumn(name = "applicant_id")],
    )
    @Column(name = "position_name")
    var positions: MutableList<String> = mutableListOf()
        protected set

    @ElementCollection
    @CollectionTable(
        name = "applicant_stacks",
        joinColumns = [JoinColumn(name = "applicant_id")],
    )
    var stacks: MutableList<String> = mutableListOf()
        protected set

    @ElementCollection
    @CollectionTable(
        name = "applicant_links",
        joinColumns = [JoinColumn(name = "applicant_id")],
    )
    var links: MutableList<LinkVo> = mutableListOf()
        protected set

    override fun getRole() = UserRole.ROLE_APPLICANT
}
