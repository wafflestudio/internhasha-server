package com.wafflestudio.internhasha.applicant.domain

import com.wafflestudio.internhasha.auth.domain.User
import com.wafflestudio.internhasha.auth.domain.UserRole
import com.wafflestudio.internhasha.common.domain.LinkVo
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table

@Entity
@Table(name = "applicants")
@DiscriminatorValue(value = "applicant")
class Applicant(
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
) : User(
        name = name,
        email = email,
        passwordHash = passwordHash,
    ) {
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "applicant_positions",
        joinColumns = [JoinColumn(name = "applicant_id")],
    )
    @Column(name = "position_name")
    var positions: MutableSet<String> = mutableSetOf()
        protected set

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "applicant_stacks",
        joinColumns = [JoinColumn(name = "applicant_id")],
    )
    var stacks: MutableSet<String> = mutableSetOf()
        protected set

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "applicant_links",
        joinColumns = [JoinColumn(name = "applicant_id")],
    )
    var links: MutableSet<LinkVo> = mutableSetOf()
        protected set

    override fun getRole() = UserRole.ROLE_APPLICANT
}
