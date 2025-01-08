package com.waffletoy.team1server.account.persistence

import com.waffletoy.team1server.post.persistence.PostEntity
import jakarta.persistence.*

@Entity(name = "admins")
@DiscriminatorValue("ADMIN")
class AdminEntity(
    @Column(name = "profile_image_link", length = 255)
    var profileImageLink: String? = null,
    override var username: String,
    override var password: String? = null,
    override var localId: String? = null,
    // 양방향 접근 - admin.posts로 postEntity 불러오기 가능!
    @OneToMany(mappedBy = "admin", cascade = [CascadeType.ALL], orphanRemoval = true)
    val posts: List<PostEntity> = mutableListOf(),
) : AccountEntity(
        username = username,
        localId = localId,
        password = password,
    )
