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
interface PostRepository : JpaRepository<PostEntity, String>, JpaSpecificationExecutor<PostEntity> {
    @Query("SELECT p FROM PostEntity p WHERE p.id IN :ids")
    fun findAllByIdIn(
        @Param("ids") ids: List<String>,
        pageable: Pageable,
    ): Page<PostEntity>
}

class PostSpecification {
    companion object {
        fun withFilters(
            roles: List<String>?,
            investmentUp: Int?,
            investmentDown: Int?,
            status: Int,
        ): Specification<PostEntity> {
            return Specification { root, query, criteriaBuilder ->

                val predicates = mutableListOf<Predicate>()

                // roles 조건
                roles?.let { it ->
                    val roleJoin = root.join<PostEntity, RoleEntity>("roles")
                    val roleEnums = it.mapNotNull { roleName -> Category.entries.find { it.name == roleName } }
                    if (roleEnums.isNotEmpty()) {
                        predicates.add(
                            criteriaBuilder.or(
                                *roleEnums.map { roleEnum ->
                                    criteriaBuilder.equal(roleJoin.get<Category>("category"), roleEnum.name)
                                }.toTypedArray(),
                            ),
                        )
                    }
                }

                // investment 조건
                investmentDown?.let {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("investAmount"), it))
                }
                investmentUp?.let {
                    predicates.add(criteriaBuilder.lessThan(root.get("investAmount"), it))
                }

                // status 조건
                status.let {
                    when (it) {
                        0 -> {
                            // 진행 중 (현재 날짜가 employmentEndDate 이전)
                            predicates.add(
                                criteriaBuilder.greaterThanOrEqualTo(root.get("employmentEndDate"), LocalDateTime.now()),
                            )
                        }
                        1 -> {
                            // 진행 완료 (현재 날짜가 employmentEndDate 이후)
                            predicates.add(
                                criteriaBuilder.lessThan(root.get("employmentEndDate"), LocalDateTime.now()),
                            )
                        }
                        2 -> {
                            // 전체 (조건 없음)
                        }

                        else -> {
                        }
                    }
                }

                // 최종 조건 조합
                criteriaBuilder.and(*predicates.toTypedArray())
            }
        }
    }
}
