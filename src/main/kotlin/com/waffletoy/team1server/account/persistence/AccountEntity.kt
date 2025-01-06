package com.waffletoy.team1server.account.persistence

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "accounts")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "account_type", discriminatorType = DiscriminatorType.STRING)
@EntityListeners(AuditingEntityListener::class)
open class AccountEntity(
    // UUID
    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    open val id: String = UUID.randomUUID().toString(),
    @Column(name = "username", nullable = false, unique = true)
    open var username: String,
    @Column(name = "local_id", unique = true)
    open var localId: String? = null,
    @Column(name = "local_password_hash", nullable = true)
    open var password: String? = null,
    @CreatedDate
    @Column(nullable = false, updatable = false)
    open var createdAt: LocalDateTime = LocalDateTime.now(),
    @LastModifiedDate
    @Column(nullable = false)
    open var updatedAt: LocalDateTime = LocalDateTime.now(),
)

// snu_mail nullable false, unique true, googleId var nullable true unique true, phoneNumber nullable true var, posts 제외됨.
// profileImageLink length 255 또한 제외.
// password, username 등의 naming 모호함 issue-> 나중에
// username -> User를 일반 유저로 한정지은 순간 username이라는 Naming이 모호해짐.
// username nullable 해야 하지 않나? 아니면 나방129 등으로 자동생성해주는 library라도 불러와야.
