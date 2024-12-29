package com.waffletoy.team1server.user.persistence

import com.waffletoy.team1server.post.persistence.PostEntity
import jakarta.persistence.*

@Entity(name = "users")
@Table(name = "users")
class UserEntity(
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
)
