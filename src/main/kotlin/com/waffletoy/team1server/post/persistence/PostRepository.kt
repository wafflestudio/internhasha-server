package com.waffletoy.team1server.post.persistence

import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface PostRepository : JpaRepository<PostEntity, String>, JpaSpecificationExecutor<PostEntity> {
//    fun findByAu/
}

class PostSpecification {
    companion object {
        fun withFilters(
            roles: List<String>?,
            investment: Int?,
            investors: List<String>?
        ): Specification<PostEntity> {
            return Specification { root, query, criteriaBuilder ->

                val predicates = mutableListOf<Predicate>()

                // roles 조건
                roles?.let {
                    val roleJoin = root.join<PostEntity, RoleEntity>("roles")
                    predicates.add(roleJoin.get<String>("name").`in`(it))
                }

                // investment 조건
                investment?.let {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("investAmount"), it))
                }

                // investors 조건
                investors?.let {
                    val investorJoin = root.join<PostEntity, InvestCompany>("investCompany")
                    predicates.add(investorJoin.get<String>("companyName").`in`(it))
                }

                // 최종 조건 조합
                criteriaBuilder.and(*predicates.toTypedArray())
            }
        }
    }
}
