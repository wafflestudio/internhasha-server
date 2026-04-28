package com.wafflestudio.internhasha.auth.security

import com.wafflestudio.internhasha.exceptions.BadAuthorizationHeaderException
import org.springframework.core.MethodParameter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class AuthUserArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.parameterType == AuthUserPrincipal::class.java &&
            parameter.hasParameterAnnotation(AuthUser::class.java)

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): AuthUserPrincipal? {
        val authentication = SecurityContextHolder.getContext().authentication
        val user = authentication?.principal as? AuthUserPrincipal

        if (user == null && !parameter.isOptional) {
            throw BadAuthorizationHeaderException(details = mapOf("Authorization" to "Missing or invalid Authorization header"))
        }

        return user
    }
}
