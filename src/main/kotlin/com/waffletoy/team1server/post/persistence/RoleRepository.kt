package com.waffletoy.team1server.post.persistence

import com.waffletoy.team1server.post.Category
import jakarta.persistence.criteria.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface RoleRepository : JpaRepository<RoleEntity, String>, JpaSpecificationExecutor<RoleEntity> {
    @Query("SELECT p FROM RoleEntity p WHERE p.id IN :ids")
    fun findAllByIdIn(
        @Param("ids") ids: List<String>,
        pageable: Pageable,
    ): Page<RoleEntity>
}

class RoleSpecification {
    companion object {
        fun withFilters(
            roles: List<String>?,
            investmentMax: Int?,
            investmentMin: Int?,
            status: Int,
            currentDateTime: LocalDateTime = LocalDateTime.now(),
        ): Specification<RoleEntity> {
            return Specification { root, query, criteriaBuilder ->

                val predicates = mutableListOf<Predicate>()

                // 상시채용의 종료일
                val endDay = LocalDateTime.of(2099, 12, 31, 23, 59)

                // roles 조건
                roles?.let {
                    val roleEnums =
                        it.mapNotNull { roleName ->
                            Category.entries.find { it.name == roleName }
                        }
                    if (roleEnums.isNotEmpty()) {
                        predicates.add(
                            criteriaBuilder.or(
                                *roleEnums.map { roleEnum ->
                                    criteriaBuilder.equal(root.get<String>("category"), roleEnum.name)
                                }.toTypedArray(),
                            ),
                        )
                    }
                }

                // investment 조건 (Company Entity와 join)
                val postJoin = root.join<RoleEntity, CompanyEntity>("company")

                // 하한
                investmentMin?.let {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(postJoin.get<Int>("investAmount"), it))
                }

                // 상한
                investmentMax?.let {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(postJoin.get<Int>("investAmount"), it))
                }

                // status 조건
                when (status) {
                    0 -> {
                        // 진행 중 (현재 날짜가 employmentEndDate 이전)
                        val employmentEndDate =
                            criteriaBuilder.coalesce(
                                root.get("employmentEndDate"),
                                endDay,
                            )
                        predicates.add(criteriaBuilder.greaterThanOrEqualTo(employmentEndDate, currentDateTime))
                    }
                    1 -> {
                        // 진행 완료 (현재 날짜가 employmentEndDate 이후)
                        val employmentEndDate =
                            criteriaBuilder.coalesce(
                                root.get("employmentEndDate"),
                                endDay,
                            )
                        predicates.add(criteriaBuilder.lessThan(employmentEndDate, currentDateTime))
                    }
                    2 -> {
                        // 전체 (조건 없음)
                    }
                    else -> {
                        throw IllegalArgumentException("Invalid status value: $status")
                    }
                }

                // 최종 조건 조합
                criteriaBuilder.and(*predicates.toTypedArray())
            }
        }
    }
}
