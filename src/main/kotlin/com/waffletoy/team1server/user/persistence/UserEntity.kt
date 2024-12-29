package com.waffletoy.team1server.user.persistence

import com.waffletoy.team1server.post.persistence.PostEntity
import com.waffletoy.team1server.user.*
import jakarta.persistence.*
import java.time.Instant

@Entity(name = "users")
@Table(name = "users")
open class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,
    @Column(name = "name", nullable = false, length = 100)
    val name: String,
    @Column(name = "email", nullable = false, unique = true, length = 100)
    val email: String,
    @Column(name = "phone_number", nullable = false, length = 15)
    val phoneNumber: String,
    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val authoredPosts: Set<PostEntity> = emptySet(),
//    @OneToMany(mappedBy = "participant", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
//    val participations: Set<ParticipationEntity> = emptySet(), // 이거 어떻게 쓸지는 고민을 해봐야겠어요
    // 해시된 비밀번호 저장
    @Column(name = "password", nullable = true)
    val password: String? = null,
    // user status
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    val status: UserStatus,
    // 어디서 로그인했는지 - local or google
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false)
    val authProvider: AuthProvider,
    // Refresh Token 저장
    @Column(name = "refresh_token", nullable = true, length = 512)
    var refreshToken: String? = null,
    // Refresh Token 만료 시간 저장
    @Column(name = "refresh_token_expires_at", nullable = true)
    var refreshTokenExpiresAt: Instant? = null,
)
