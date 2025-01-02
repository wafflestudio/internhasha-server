package com.waffletoy.team1server.user

import com.waffletoy.team1server.user.controller.AuthenticatedUser
import com.waffletoy.team1server.user.controller.User
import com.waffletoy.team1server.user.service.UserService
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class UserArgumentResolver(
    private val userService: UserService,
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.parameterType == User::class.java &&
            parameter.hasParameterAnnotation(AuthUser::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): AuthenticatedUser? {
        return runCatching {
            val accessToken =
                requireNotNull(
                    webRequest.getHeader("Authorization")?.split(" ")?.let {
                        if (it.getOrNull(0) == "Bearer") it.getOrNull(1) else null
                    },
                )
            userService.authenticate(accessToken, null)
        }.getOrElse {
            if (parameter.hasParameterAnnotation(AuthUser::class.java)) {
                throw AuthenticateException()
            } else {
                null
            }
        }
    }
}
