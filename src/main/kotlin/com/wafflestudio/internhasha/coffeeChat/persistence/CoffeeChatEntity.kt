package com.wafflestudio.internhasha.coffeeChat.persistence

import com.wafflestudio.internhasha.auth.persistence.UserEntity
import com.wafflestudio.internhasha.coffeeChat.CoffeeChatStatus
import com.wafflestudio.internhasha.post.persistence.PositionEntity
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "coffeeChats")
class CoffeeChatEntity(
    @Id
    @Column(name = "ID", nullable = false)
    open val id: String = UUID.randomUUID().toString(),
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    open var createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "UPDATED_AT", nullable = false)
    open var updatedAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "CONTENT", columnDefinition = "TEXT", nullable = false)
    open var content: String,
    @Column(name = "COFFEE_CHAT_STATUS", nullable = false)
    open var coffeeChatStatus: CoffeeChatStatus = CoffeeChatStatus.WAITING,
    @Column(name = "CHANGED", nullable = false)
    open var changed: Boolean = false,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "POSITION_ID", nullable = false)
    open var position: PositionEntity,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "APPLICANT_ID", nullable = false)
    open val applicant: UserEntity,
) {
    @PrePersist
    fun onCreate() {
        createdAt = LocalDateTime.now()
        updatedAt = createdAt
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
