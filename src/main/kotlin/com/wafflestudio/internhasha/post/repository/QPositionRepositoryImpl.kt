package com.wafflestudio.internhasha.post.repository

import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflestudio.internhasha.company.domain.Domain.Companion.fromString
import com.wafflestudio.internhasha.company.domain.QCompany
import com.wafflestudio.internhasha.post.domain.Category
import com.wafflestudio.internhasha.post.domain.Position
import com.wafflestudio.internhasha.post.domain.QPosition
import com.wafflestudio.internhasha.post.service.dto.PositionSearchCond
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class QPositionRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
// )
) : QPositionRepository {
    private val position = QPosition.position
    private val company = QCompany.company

    companion object {
        private val MAX_END_DATE = LocalDateTime.of(2099, 12, 31, 23, 59)
    }

    override fun searchPosition(
        cond: PositionSearchCond,
        pageable: Pageable,
    ): Page<Position> {
        val countQuery =
            queryFactory
                .select(position.count())
                .from(position)
                .innerJoin(position.company, company)
                .where(
                    categoryIn(cond.positions),
                    companyIdEq(cond.companyId),
                    isActiveCond(cond.isActive, cond.currentDateTime),
                    domainIn(cond.domains),
                )

        val content =
            queryFactory
                .selectFrom(position)
                .innerJoin(position.company, company)
                .fetchJoin()
                .where(
                    categoryIn(cond.positions),
                    companyIdEq(cond.companyId),
                    isActiveCond(cond.isActive, cond.currentDateTime),
                    domainIn(cond.domains),
                ).orderBy(*createOrderSpecifiers(cond.order, cond.currentDateTime))
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()

        return PageableExecutionUtils.getPage(content, pageable) {
            countQuery.fetchOne() ?: 0L
        }
    }

    private fun categoryIn(positions: List<String>?): BooleanExpression? {
        if (positions.isNullOrEmpty()) return null
        val enums = positions.mapNotNull { Category.fromString(it) }
        return if (enums.isEmpty()) null else position.positionType.`in`(enums)
    }

    private fun companyIdEq(companyId: String?): BooleanExpression? = companyId?.let { position.company.id.eq(it) }

    private fun isActiveCond(
        isActive: Boolean,
        now: LocalDateTime,
    ): BooleanExpression? {
        if (!isActive) return null
        val endDate = position.employmentEndDate.coalesce(MAX_END_DATE)
        return endDate.gt(now)
    }

    private fun domainIn(domains: List<String>?): BooleanExpression? {
        if (domains.isNullOrEmpty()) return null
        val enums = domains.mapNotNull { fromString(it) }
        return if (enums.isEmpty()) null else company.domain.`in`(enums)
    }

    private fun createOrderSpecifiers(
        order: Int,
        now: LocalDateTime,
    ): Array<OrderSpecifier<*>> {
        val endDate = position.employmentEndDate.coalesce(MAX_END_DATE)

        return when (order) {
            1 -> {
                // 1. 마감 여부 우선순위 (마감 전=1, 마감 후=2)
                val statusStep =
                    CaseBuilder()
                        .`when`(endDate.gt(now))
                        .then(1)
                        .otherwise(2)

                // 2. 마감 전인 건 마감일 오름차순(가까운 마감일 순)
                val detailSort =
                    CaseBuilder()
                        .`when`(endDate.gt(now))
                        .then(endDate)
                        .otherwise(null as LocalDateTime?) // 마감 후는 정렬 제외

                // 그룹 우선순위
                // 마감 안 된 건 빨리 마감되는 순
                // 마감된 건 최근에 마감된 순
                arrayOf(
                    statusStep.asc(),
                    detailSort.asc(),
                    endDate.desc(),
                )
            }

            // 기본은 생성일 오름차순
            else -> arrayOf(position.createdAt.asc())
        }
    }
}
