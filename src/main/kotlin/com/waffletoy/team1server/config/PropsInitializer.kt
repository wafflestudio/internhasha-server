package com.waffletoy.team1server.config

import com.waffletoy.team1server.auth.utils.UserTokenUtil
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class PropsInitializer(
    @Value("\${custom.TOKEN_PRIVATE_KEY}") private val tokenKey: String,
) {
    @PostConstruct
    fun init() {
        UserTokenUtil.initFromSpring(tokenKey)
    }
}
