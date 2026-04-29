package com.wafflestudio.internhasha.company.domain

import com.wafflestudio.internhasha.auth.domain.User
import com.wafflestudio.internhasha.auth.domain.UserRole
import com.wafflestudio.internhasha.common.domain.LinkVo
import com.wafflestudio.internhasha.common.domain.TagVo
import com.wafflestudio.internhasha.post.domain.Position
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "companies")
@DiscriminatorValue(value = "company")
class Company(
    name: String,
    email: String,
    passwordHash: String? = null,
    var companyEstablishedYear: Int? = null,
    @Enumerated(EnumType.STRING)
    var domain: Domain? = null,
    var headcount: Int? = null,
    var location: String? = null,
    var slogan: String? = null,
    @Column(columnDefinition = "TEXT")
    var detail: String? = null,
    var profileImageKey: String? = null,
    @Column(length = 2048)
    var companyInfoPDFKey: String? = null,
    @Column(length = 2048)
    var landingPageLink: String? = null,
    var vcName: String? = null,
    var vcRec: String? = null,
) : User(
        name = name,
        email = email,
        passwordHash = passwordHash,
    ) {
    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    var positions: MutableSet<Position> = mutableSetOf()
        protected set

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "company_links",
        joinColumns = [JoinColumn(name = "company_id")],
    )
    var links: MutableSet<LinkVo> = mutableSetOf()
        protected set

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "company_tags",
        joinColumns = [JoinColumn(name = "company_id")],
    )
    var tags: MutableSet<TagVo> = mutableSetOf()
        protected set

    override fun getRole() = UserRole.ROLE_COMPANY
}
