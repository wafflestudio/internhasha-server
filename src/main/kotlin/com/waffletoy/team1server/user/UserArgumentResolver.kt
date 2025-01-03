package com.waffletoy.team1server.user

import com.waffletoy.team1server.user.controller.User
import com.waffletoy.team1server.user.service.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
    private val logger: Logger = LoggerFactory.getLogger(UserArgumentResolver::class.java)

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        val isSupported =
            parameter.parameterType == User::class.java &&
                parameter.hasParameterAnnotation(AuthUser::class.java)
        logger.info("supportsParameter called: $isSupported")
        return isSupported
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): User? {
        logger.info("resolveArgument called for parameter: ${parameter.parameterName}")

        val authorizationHeader = webRequest.getHeader("Authorization")
        logger.info("Authorization Header: $authorizationHeader")

        return runCatching {
            val accessToken =
                requireNotNull(
                    authorizationHeader?.split(" ")?.let {
                        if (it.getOrNull(0) == "Bearer") it.getOrNull(1) else null
                    },
                ) {
                    "Authorization header is missing or invalid"
                }
            logger.info("Extracted Access Token: $accessToken")

            userService.authenticate(accessToken).also {
                logger.info("Authenticated User: $it")
            }
        }.getOrElse {
            logger.error("Error during resolveArgument: ${it.message}", it)
            if (parameter.hasParameterAnnotation(AuthUser::class.java)) {
                throw AuthenticateException()
            } else {
                null
            }
        }
    }
}

// @Component
// class UserArgumentResolver(
//    private val userService: UserService,
// ) : HandlerMethodArgumentResolver {
//    override fun supportsParameter(parameter: MethodParameter): Boolean {
//        return parameter.parameterType == AuthenticatedUser::class.java &&
//            parameter.hasParameterAnnotation(AuthUser::class.java)
//    }
//
//    override fun resolveArgument(
//        parameter: MethodParameter,
//        mavContainer: ModelAndViewContainer?,
//        webRequest: NativeWebRequest,
//        binderFactory: WebDataBinderFactory?,
//    ): User? {
//        return runCatching {
//            val accessToken =
//                requireNotNull(
//                    webRequest.getHeader("Authorization")?.split(" ")?.let {
//                        if (it.getOrNull(0) == "Bearer") it.getOrNull(1) else null
//                    },
//                )
//            userService.authenticate(accessToken)
//        }.getOrElse {
//            if (parameter.hasParameterAnnotation(AuthUser::class.java)) {
//                throw AuthenticateException()
//            } else {
//                null
//            }
//        }
//    }
// }
