package com.waffletoy.team1server.post.persistence

import com.waffletoy.team1server.post.Category
import com.waffletoy.team1server.post.PostInvalidFiltersException
import com.waffletoy.team1server.post.Series
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Join
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDateTime

class PositionSpecification {
    companion object {
        fun withFilters(
            positions: List<String>?,
            investmentMax: Int?,
            investmentMin: Int?,
            status: Int,
            series: List<String>?,
            currentDateTime: LocalDateTime = LocalDateTime.now(),
        ): Specification<PositionEntity> {
            return Specification { root, query, criteriaBuilder ->

                val endDay = LocalDateTime.of(2099, 12, 31, 23, 59)

                val companyJoin = root.join<PositionEntity, CompanyEntity>("company")

                val predicates =
                    listOfNotNull(
                        buildCategoryPredicate(root, criteriaBuilder, positions),
                        buildSeriesPredicate(companyJoin, criteriaBuilder, series),
                        buildInvestmentMinPredicate(companyJoin, criteriaBuilder, investmentMin),
                        buildInvestmentMaxPredicate(companyJoin, criteriaBuilder, investmentMax),
                        buildStatusPredicate(root, criteriaBuilder, status, currentDateTime, endDay),
                    )

                criteriaBuilder.and(*predicates.toTypedArray())
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

        private fun buildSeriesPredicate(
            companyJoin: Join<PositionEntity, CompanyEntity>,
            criteriaBuilder: CriteriaBuilder,
            series: List<String>?,
        ): Predicate? {
            series?.let {
                val seriesEnums =
                    series.mapNotNull { seriesString ->
                        Series.entries.find { c -> c.name == seriesString }
                    }
                if (seriesEnums.isNotEmpty()) {
                    return criteriaBuilder.or(
                        *seriesEnums.map { seriesEnum ->
                            criteriaBuilder.equal(companyJoin.get<String>("series"), seriesEnum.name)
                        }.toTypedArray(),
                    )
                }
            }
            return null
        }

        private fun buildInvestmentMinPredicate(
            companyJoin: Join<PositionEntity, CompanyEntity>,
            criteriaBuilder: CriteriaBuilder,
            investmentMin: Int?,
        ): Predicate? {
            return investmentMin?.let {
                criteriaBuilder.greaterThanOrEqualTo(companyJoin.get<Int>("investAmount"), it)
            }
        }

        private fun buildInvestmentMaxPredicate(
            companyJoin: Join<PositionEntity, CompanyEntity>,
            criteriaBuilder: CriteriaBuilder,
            investmentMax: Int?,
        ): Predicate? {
            return investmentMax?.let {
                criteriaBuilder.lessThanOrEqualTo(companyJoin.get<Int>("investAmount"), it)
            }
        }

        private fun buildStatusPredicate(
            root: Root<PositionEntity>,
            criteriaBuilder: CriteriaBuilder,
            status: Int,
            currentDateTime: LocalDateTime,
            endDay: LocalDateTime,
        ): Predicate? {
            val employmentEndDate =
                criteriaBuilder.coalesce(
                    root.get("employmentEndDate"),
                    endDay,
                )
            return when (status) {
                0 -> criteriaBuilder.greaterThanOrEqualTo(employmentEndDate, currentDateTime) // 진행 중
                1 -> criteriaBuilder.lessThan(employmentEndDate, currentDateTime) // 진행 완료
                2 -> null // 조건 없음
                else -> throw PostInvalidFiltersException(details = mapOf("status" to status))
            }
        }
    }
}
