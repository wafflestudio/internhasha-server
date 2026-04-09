package com.wafflestudio.internhasha.coffeeChat.persistence

import com.wafflestudio.internhasha.applicant.persistence.ApplicantEntity
import com.wafflestudio.internhasha.coffeeChat.domain.CoffeeChatStatus
import com.wafflestudio.internhasha.common.persistence.BaseEntity
import com.wafflestudio.internhasha.post.persistence.PositionEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class CoffeeChatEntity(
    @Column(columnDefinition = "TEXT")
    var content: String,
    applicantEntity: ApplicantEntity,
    positionEntity: PositionEntity,
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "coffeechat_id")
    var id: String? = null
        protected set

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id")
    var applicant: ApplicantEntity = applicantEntity
        protected set

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "position_id")
    var position: PositionEntity = positionEntity
        protected set

    @Enumerated(EnumType.STRING)
    var coffeeChatStatus: CoffeeChatStatus = CoffeeChatStatus.WAITING
        protected set
    var changed: Boolean = false
        protected set

    fun updateStatus(newStatus: CoffeeChatStatus) {
        coffeeChatStatus = newStatus
        changed = true
    }

    fun markAsRead() {
        changed = false
    }
}
