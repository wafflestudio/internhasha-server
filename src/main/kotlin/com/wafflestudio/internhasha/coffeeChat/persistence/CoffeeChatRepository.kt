package com.wafflestudio.internhasha.coffeeChat.persistence

import com.wafflestudio.internhasha.coffeeChat.domain.CoffeeChatStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CoffeeChatRepository : JpaRepository<CoffeeChatEntity, String> {
    @Query(
        """
        SELECT cc FROM CoffeeChatEntity cc
        JOIN FETCH cc.position p
        JOIN FETCH p.company c
        JOIN FETCH cc.applicant a
        WHERE cc.id = :coffeeChatId
    """,
    )
    fun findByIdOrNull(coffeeChatId: String): CoffeeChatEntity?

    @Query(
        """
        SELECT cc FROM CoffeeChatEntity cc
        JOIN FETCH cc.position p
        JOIN FETCH p.company c
        JOIN FETCH cc.applicant a
        WHERE a.id = :applicantId
          AND p.id = :positionId
          AND cc.coffeeChatStatus = :status
    """,
    )
    fun findCoffeeChatByFilter(
        applicantId: String,
        positionId: String,
        status: CoffeeChatStatus,
    ): CoffeeChatEntity?

    fun existsByApplicantIdAndPositionIdAndCoffeeChatStatus(
        applicantId: String,
        positionId: String,
        status: CoffeeChatStatus,
    ): Boolean

    @Query(
        """
        SELECT cc FROM CoffeeChatEntity cc
        JOIN FETCH cc.position p
        JOIN FETCH p.company c
        JOIN FETCH cc.applicant a
        WHERE a.id = :applicantId
    """,
    )
    fun findAllByApplicantId(applicantId: String): List<CoffeeChatEntity>

    // 취소된 커피챗 제외, 대상 회사의 모든 커피챗을 가져오기
    @Query(
        """
        SELECT cc
        FROM CoffeeChatEntity cc
        JOIN FETCH cc.position p
        JOIN FETCH p.company c
        JOIN FETCH cc.applicant a
        WHERE c.id = :companyId
        AND cc.coffeeChatStatus <> :excludedStatus
    """,
    )
    fun findAllExceptStatusByCompanyId(
        @Param("companyId") companyId: String,
        @Param("excludedStatus") excludedStatus: CoffeeChatStatus,
    ): List<CoffeeChatEntity>

    // 지원자 - isChanged 개수 가져오기
    fun countByApplicantIdAndChangedTrue(applicantId: String): Long

    // 회사 - 대기 중 개수 가져오기
    @Query(
        """
        SELECT COUNT(cc) 
        FROM CoffeeChatEntity cc
        WHERE cc.position.company.id = :companyId
        AND cc.coffeeChatStatus = :status
    """,
    )
    fun countByCompanyIdAndStatus(
        @Param("companyId") companyId: String,
        @Param("status") status: CoffeeChatStatus,
    ): Long

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM CoffeeChatEntity c WHERE c.applicant.id = :applicantId")
    fun deleteAllByApplicantId(
        @Param("applicantId") applicantId: String,
    )
}
