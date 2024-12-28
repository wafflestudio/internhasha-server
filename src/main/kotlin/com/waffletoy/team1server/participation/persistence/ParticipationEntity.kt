package com.waffletoy.team1server.participation.persistence

import com.waffletoy.team1server.post.persistence.PostEntity
import com.waffletoy.team1server.user.persistence.UserEntity
import jakarta.persistence.*
import java.time.Instant

@Entity(name = "participations")
@Table(name = "participations")
class ParticipationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,
//    @ManyToOne
//    @JoinColumn(name = "author_id", nullable = false)
//    val author: UserEntity,
    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    val post: PostEntity,
    @ManyToOne
    @JoinColumn(name = "participant_id", nullable = true)
    val participant: UserEntity? = null,
    @Column(name = "phone_number", length = 20, nullable = false)
    val phoneNumber: String,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
)
