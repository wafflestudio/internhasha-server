package com.waffletoy.team1server.account.persistence

import com.waffletoy.team1server.resume.persistence.ResumeEntity
import jakarta.persistence.*

@Entity(name = "users")
@DiscriminatorValue("USER")
class UserEntity(
    @Column(name = "google_Id", nullable = true, unique = true)
    var googleId: String? = null,
    @Column(name = "phone_number", nullable = true)
    var phoneNumber: String? = null,
    username: String,
    localId: String? = null,
    password: String? = null,
    snuMail: String,
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val resumes: List<ResumeEntity> = mutableListOf(),
    // @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    // val posts: List<PostEntity> = mutableListOf()
) : AccountEntity(
        username = username,
        localId = localId,
        password = password,
        snuMail = snuMail,
    )
// 부모의 생성자로 넘어가는 변수는 정의하지 않음
// AccountEntity, AdminEntity에서 snumail 값이 없기 때문에 기본값 "" 설정 필요
// ResumeEntity와 join
