package com.waffletoy.team1server.coffeeChat.persistence

import com.waffletoy.team1server.coffeeChat.CoffeeChatStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CoffeeChatRepository : JpaRepository<CoffeeChatEntity, String> {
    fun findAllByApplicantId(applicantId: String): List<CoffeeChatEntity>

    // 취소된 커피챗 제외, 대상 회사의 모든 커피챗을 가져오기
    @Query(
        """
    SELECT c 
    FROM CoffeeChatEntity c 
    WHERE c.position.company.user.id = :userId
    AND c.coffeeChatStatus <> :excludedStatus
""",
    )
    fun findAllExceptStatusByUserId(
        @Param("userId") userId: String,
        @Param("excludedStatus") excludedStatus: CoffeeChatStatus,
    ): List<CoffeeChatEntity>

    fun deleteAllByApplicantId(applicantId: String)

    // 지원자 - isChanged 개수 가져오기
    fun countByApplicantIdAndChangedTrue(applicantId: String): Long

    // 지원자 - 해당 공고에 이미 신청한 커피챗 가져오기
    fun findByApplicantIdAndPositionIdAndCoffeeChatStatus(
        userId: String,
        positionId: String,
        coffeeChatStatus: CoffeeChatStatus,
    ): CoffeeChatEntity?

    // 회사 - 대기 중 개수 가져오기
    @Query(
        """
    SELECT COUNT(c) 
    FROM CoffeeChatEntity c 
    WHERE c.position.company.user.id = :userId
    AND c.coffeeChatStatus = :status
    """,
    )
    fun countByUserIdAndStatus(
        @Param("userId") userId: String,
        @Param("status") status: CoffeeChatStatus,
    ): Long

    //특정 공고에 대해 신청된 커피수(상태 무관) 개수 가져오기
    fun countByPositionId(positionId: String): Long
}
