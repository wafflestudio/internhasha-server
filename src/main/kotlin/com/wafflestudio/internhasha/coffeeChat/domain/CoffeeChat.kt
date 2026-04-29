package com.wafflestudio.internhasha.coffeeChat.domain

import com.wafflestudio.internhasha.applicant.domain.Applicant
import com.wafflestudio.internhasha.common.domain.BaseEntity
import com.wafflestudio.internhasha.post.domain.Position
import jakarta.persistence.CascadeType
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
class CoffeeChat(
    @Column(columnDefinition = "TEXT")
    var content: String,
    applicant: Applicant,
    position: Position,
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "coffeechat_id")
    var id: String? = null
        protected set

    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = [CascadeType.DETACH])
    @JoinColumn(name = "applicant_id")
    var applicant: Applicant = applicant
        protected set

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "position_id")
    var position: Position = position
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

    fun requireId(): String = requireNotNull(id) { "Entity Id must not be null" }
}
