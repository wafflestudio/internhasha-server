package com.waffletoy.team1server.account.persistence

import com.waffletoy.team1server.post.persistence.PostEntity
import jakarta.persistence.*

@Entity(name = "admins")
@DiscriminatorValue("ADMIN")
class AdminEntity(
    @Column(name = "profile_image_link", length = 255)
    var profileImageLink: String? = null,
    username: String,
    password: String? = null,
    localId: String? = null,
    @OneToMany(mappedBy = "admin", cascade = [CascadeType.ALL], orphanRemoval = true)
    val posts: List<PostEntity> = mutableListOf(),
) : AccountEntity(
        username = username,
        localId = localId,
        password = password,
    )
