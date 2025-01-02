package com.waffletoy.team1server.user.persistence

import com.waffletoy.team1server.post.persistence.PostEntity
import com.waffletoy.team1server.user.*
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.Instant
import java.util.*

@Entity(name = "users")
@Table(name = "users")
open class UserEntity(
    // 자동 생성되는 User의 ID(36자의 고정 길이 문자열)
    @Id
    @Column(length = 36)
    val id: String = UUID.randomUUID().toString(),
    // 유저의 스누메일
    @Column(name = "snu_mail", nullable = false, unique = true)
    val snuMail: String,
    // 유저 표시 이름
    @Column(name = "nickname", nullable = false)
    var nickname: String,
    // 계정 상태(활성화, 비활성화)
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    var status: UserStatus,
    // 로그인 방법 - local or google
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false)
    var authProvider: AuthProvider,
    // 로그인 아이디
    @Column(name = "login_id", nullable = true, unique = true)
    val loginId: String? = null,
    // 해시된 비밀번호 저장
    @Column(name = "password", nullable = true)
    var password: String? = null,
    // 추가적인 소셜 로그인 정보
    // 구글 ID
    @Column(name = "google_id", nullable = true, unique = true)
    val googleId: String? = null,
    // 구글 이메일
    @Column(name = "google_email", nullable = false, unique = true)
    val googleEmail: String? = null,
    // 추가적인 개인 정보
    // 생성, 수정 시간
    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),
    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: Instant = Instant.now(),
    // 작성한 포스트
    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val authoredPosts: Set<PostEntity> = emptySet(),
    //    @OneToMany(mappedBy = "participant", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    //    val participations: Set<ParticipationEntity> = emptySet(), // 이거 어떻게 쓸지는 고민을 해봐야겠어요
)
