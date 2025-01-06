package com.waffletoy.team1server.account.persistence

import jakarta.persistence.*

@Entity(name = "users")
@DiscriminatorValue("USER")
class UserEntity(
    @Column(name = "snu_mail", nullable = false, unique = true)
    val snuMail: String,
    @Column(name = "google_Id", nullable = true, unique = true)
    var googleId: String? = null,
    @Column(name = "phone_number", nullable = true)
    var phoneNumber: String? = null,
    override var username: String,
    override var localId: String? = null,
    override var password: String? = null,
    // @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    // val posts: List<PostEntity> = mutableListOf()
) : AccountEntity(
        username = username,
        localId = localId,
        password = password,
    )
