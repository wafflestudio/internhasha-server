package com.waffletoy.team1server.user.persistence

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.*

@Entity(name = "users")
@Table(name = "users")
@EntityListeners(AuditingEntityListener::class)
open class UserEntity(
    // 자동 생성되는 User의 ID(36자의 고정 길이 문자열)
    @Id
    @Column(length = 36)
    val id: String = UUID.randomUUID().toString(),
    // 유저의 스누메일
    @Column(name = "snu_mail", nullable = false, unique = true)
    val snuMail: String,
    // 유저 표시 이름
    @Column(name = "username", nullable = false)
    var username: String,
    // 로컬 아이디
    @Column(name = "local_id", nullable = true, unique = true)
    val localId: String? = null,
    // 해시된 비밀번호 저장
    @Column(name = "password", nullable = true)
    var password: String? = null,
    // 구글 ID
    @Column(name = "google_id", nullable = true, unique = true)
    val googleId: String? = null,
    // 전화번호
    @Column(name = "phone_number", nullable = true)
    val phoneNumber: String? = null,
    // 생성, 수정 시간
    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),
    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: Instant = Instant.now(),
)
