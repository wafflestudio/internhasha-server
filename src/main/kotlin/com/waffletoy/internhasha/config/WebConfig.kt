package com.waffletoy.internhasha.config

import com.waffletoy.internhasha.auth.UserArgumentResolver
import com.waffletoy.internhasha.auth.UserOrNullArgumentResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val userArgumentResolver: UserArgumentResolver,
    private val userOrNullArgumentResolver: UserOrNullArgumentResolver,
) : WebMvcConfigurer {
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(userArgumentResolver)
        resolvers.add(userOrNullArgumentResolver)
    }
}
