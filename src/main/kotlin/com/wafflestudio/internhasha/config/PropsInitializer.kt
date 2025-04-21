package com.wafflestudio.internhasha.config

import com.wafflestudio.internhasha.auth.utils.UserTokenUtil
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
