package com.wafflestudio.internhasha.post.persistence

import com.wafflestudio.internhasha.auth.persistence.UserEntity
import com.wafflestudio.internhasha.company.Domain
import com.wafflestudio.internhasha.company.persistence.CompanyEntity
import com.wafflestudio.internhasha.post.Category
import jakarta.persistence.criteria.*
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDateTime

class PositionSpecification {
    companion object {
        fun withFilters(
            positions: List<String>?,
            order: Int,
            company: UserEntity? = null,
            currentDateTime: LocalDateTime = LocalDateTime.now(),
            isActive: Boolean,
            domains: List<String>?,
        ): Specification<PositionEntity> {
            return Specification { root, query, criteriaBuilder ->
                requireNotNull(query) { "Criteria Query should not be null" }

                val endDay = LocalDateTime.of(2099, 12, 31, 23, 59)

                val companyJoin = root.join<PositionEntity, CompanyEntity>("company")

                val predicates =
                    listOfNotNull(
                        buildCategoryPredicate(root, criteriaBuilder, positions),
                        buildCompanyPredicate(root, criteriaBuilder, company),
                        buildIsActivePredicate(root, criteriaBuilder, isActive, currentDateTime, endDay),
                        buildDomainPredicate(root, criteriaBuilder, domains),
                    )

                // 중복 방지
                query.distinct(true)

                // 정렬 추가
                sortPredicate(root, criteriaBuilder, query, order, currentDateTime, endDay)

                // where 조건 설정
                if (predicates.isNotEmpty()) {
                    query.where(*predicates.toTypedArray())
                }

                // `Predicate?` 반환
                criteriaBuilder.and(*predicates.toTypedArray())
            }
        }

        private fun buildCompanyPredicate(
            root: Root<PositionEntity>,
            criteriaBuilder: CriteriaBuilder,
            company: UserEntity?,
        ): Predicate? {
            return company?.let {
                val positionJoin = root.join<PositionEntity, CompanyEntity>("company")
                val companyJoin = positionJoin.join<CompanyEntity, UserEntity>("user")

                criteriaBuilder.equal(companyJoin.get<Long>("id"), it.id)
            }
        }

        private fun buildCategoryPredicate(
            root: Root<PositionEntity>,
            criteriaBuilder: CriteriaBuilder,
            positions: List<String>?,
        ): Predicate? {
            positions?.let {
                val positionEnums =
                    positions.mapNotNull { positionName ->
                        Category.entries.find { c -> c.name == positionName }
                    }
                if (positionEnums.isEmpty()) {
                    return criteriaBuilder.disjunction()
                }
                return criteriaBuilder.or(
                    *positionEnums.map { positionEnum ->
                        criteriaBuilder.equal(root.get<Category>("positionType"), positionEnum)
                    }.toTypedArray(),
                )
            }
            return null
        }

        private fun buildIsActivePredicate(
            root: Root<PositionEntity>,
            criteriaBuilder: CriteriaBuilder,
            isActive: Boolean,
            currentDateTime: LocalDateTime,
            endDay: LocalDateTime,
        ): Predicate? {
            val employmentEndDate =
                criteriaBuilder.coalesce(
                    root.get<LocalDateTime>("employmentEndDate"),
                    endDay,
                )
            return if (isActive) {
                criteriaBuilder.greaterThan(employmentEndDate, currentDateTime)
            } else {
                null
            }
        }

        private fun buildDomainPredicate(
            root: Root<PositionEntity>,
            criteriaBuilder: CriteriaBuilder,
            domains: List<String>?,
        ): Predicate? {
            domains?.let {
                val domainEnums =
                    domains.mapNotNull { domainStr ->
                        try {
                            Domain.valueOf(domainStr)
                        } catch (e: IllegalArgumentException) {
                            null
                        }
                    }
                if (domainEnums.isEmpty()) {
                    return criteriaBuilder.disjunction() // Always false predicate
                }
                val companyJoin = root.join<PositionEntity, CompanyEntity>("company")
                return criteriaBuilder.or(
                    *domainEnums.map { domain ->
                        criteriaBuilder.equal(companyJoin.get<Domain>("domain"), domain)
                    }.toTypedArray(),
                )
            }
            return null
        }

        private fun <T> sortPredicate(
            root: Root<PositionEntity>,
            criteriaBuilder: CriteriaBuilder,
            query: CriteriaQuery<T>,
            order: Int,
            currentDateTime: LocalDateTime,
            endDay: LocalDateTime,
        ) {
            val employmentEndDate =
                criteriaBuilder.coalesce(
                    root.get<LocalDateTime>("employmentEndDate"),
                    endDay,
                )

            val orderList = mutableListOf<Order>()

            when (order) {
                1 -> {
                    // 마감순 정렬: 마감 안 된 건 오름차순, 마감된 건 내림차순
                    val caseExpression =
                        criteriaBuilder.selectCase<Int>()
                            .`when`(criteriaBuilder.greaterThan(employmentEndDate, currentDateTime), criteriaBuilder.literal(1))
                            .otherwise(criteriaBuilder.literal(2))

                    // 그룹 1: 마감 안 된 공고 → employmentEndDate ASC
                    val openSort =
                        criteriaBuilder.selectCase<LocalDateTime>()
                            .`when`(
                                criteriaBuilder.greaterThan(employmentEndDate, currentDateTime),
                                employmentEndDate,
                            )
                            .otherwise(criteriaBuilder.nullLiteral(LocalDateTime::class.java))

                    // 그룹 2: 마감된 공고 → employmentEndDate DESC
                    val closedSort =
                        criteriaBuilder.selectCase<LocalDateTime>()
                            .`when`(
                                criteriaBuilder.lessThanOrEqualTo(employmentEndDate, currentDateTime),
                                employmentEndDate,
                            )
                            .otherwise(criteriaBuilder.nullLiteral(LocalDateTime::class.java))

                    orderList.add(criteriaBuilder.asc(caseExpression)) // 그룹 우선순위 (1, 2)
                    orderList.add(criteriaBuilder.asc(openSort)) // 마감 안 된 건 오름차순
                    orderList.add(criteriaBuilder.desc(closedSort)) // 마감된 건 내림차순
                }
                else -> {
                    // 공고 생성순 정렬 (createdAt 기준)
                    orderList.add(criteriaBuilder.asc(root.get<LocalDateTime>("createdAt")))
                }
            }

            query.orderBy(orderList)
        }
    }
}
