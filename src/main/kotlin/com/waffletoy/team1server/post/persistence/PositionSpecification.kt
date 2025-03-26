package com.waffletoy.team1server.post.persistence

import com.waffletoy.team1server.auth.persistence.UserEntity
import com.waffletoy.team1server.company.persistence.CompanyEntity
import com.waffletoy.team1server.post.Category
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
        ): Specification<PositionEntity> {
            return Specification { root, query, criteriaBuilder ->
                requireNotNull(query) { "Criteria Query should not be null" }

                val endDay = LocalDateTime.of(2099, 12, 31, 23, 59)

                val companyJoin = root.join<PositionEntity, CompanyEntity>("company")

                val predicates =
                    listOfNotNull(
                        buildCategoryPredicate(root, criteriaBuilder, positions),
                        buildCompanyPredicate(root, criteriaBuilder, company),
                    )

                // 중복 방지
                query.distinct(true)

                // 정렬 추가
                sortPredicate(root, criteriaBuilder, query, order, currentDateTime)

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
                if (positionEnums.isNotEmpty()) {
                    return criteriaBuilder.or(
                        *positionEnums.map { positionEnum ->
                            criteriaBuilder.equal(root.get<String>("category"), positionEnum.name)
                        }.toTypedArray(),
                    )
                }
            }
            return null
        }

        private fun <T> sortPredicate(
            root: Root<PositionEntity>,
            criteriaBuilder: CriteriaBuilder,
            query: CriteriaQuery<T>,
            order: Int,
            currentDateTime: LocalDateTime,
        ) {
            val employmentEndDate =
                criteriaBuilder.coalesce(
                    root.get<LocalDateTime>("employmentEndDate"),
                    LocalDateTime.of(2099, 12, 31, 23, 59),
                )

            val orderList = mutableListOf<Order>()

            when (order) {
                1 -> {
                    // 마감순 정렬 (CASE WHEN을 Criteria API로 구현)
                    val caseExpression =
                        criteriaBuilder.selectCase<Int>()
                            .`when`(criteriaBuilder.greaterThan(employmentEndDate, currentDateTime), criteriaBuilder.literal(1))
                            .otherwise(criteriaBuilder.literal(2))

                    orderList.add(criteriaBuilder.asc(caseExpression))
                    orderList.add(criteriaBuilder.asc(employmentEndDate))
                }
                else -> {
                    // 최신순 정렬 (updatedAt 기준)
                    orderList.add(criteriaBuilder.desc(root.get<LocalDateTime>("updatedAt")))
                }
            }

            query.orderBy(orderList)
        }
    }
}
