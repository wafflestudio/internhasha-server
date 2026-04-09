package com.wafflestudio.internhasha.company.persistence

import com.wafflestudio.internhasha.common.domain.Link
import com.wafflestudio.internhasha.company.domain.Company
import com.wafflestudio.internhasha.company.domain.Tag
import org.springframework.stereotype.Component

@Component
class CompanyPersistenceMapper {
    fun toCompany(entity: CompanyEntity) =
        Company(
            id = requireNotNull(entity.id) { "ComanyEntity Id must not be null" },
            companyName = entity.name,
            companyEstablishedYear = entity.companyEstablishedYear,
            domain = entity.domain,
            headcount = entity.headcount,
            location = entity.location,
            slogan = entity.slogan,
            detail = entity.detail,
            profileImageKey = entity.profileImageKey,
            companyInfoPDFKey = entity.companyInfoPDFKey,
            landingPageLink = entity.landingPageLink,
            vcName = entity.vcName,
            vcRec = entity.vcRec,
            links = entity.links.map { Link(link = it.link, description = it.description) },
            tags = entity.tags.map { Tag(tag = it.tag) },
        )
}
