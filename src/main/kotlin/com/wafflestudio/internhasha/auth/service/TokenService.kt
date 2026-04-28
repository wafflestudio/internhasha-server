package com.wafflestudio.internhasha.auth.service

import com.wafflestudio.internhasha.auth.exception.UserNotFoundException
import com.wafflestudio.internhasha.auth.repository.AuthRedisRepository
import com.wafflestudio.internhasha.auth.repository.UserRepository
import com.wafflestudio.internhasha.auth.security.AuthUserPrincipal
import com.wafflestudio.internhasha.auth.security.TokenProvider
import com.wafflestudio.internhasha.auth.service.dto.Tokens
import com.wafflestudio.internhasha.auth.service.dto.UserSimple
import com.wafflestudio.internhasha.exceptions.InvalidAccessTokenException
import com.wafflestudio.internhasha.exceptions.InvalidRefreshTokenException
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class TokenService(
    private val userRepository: UserRepository,
    private val authRedisRepository: AuthRedisRepository,
    private val tokenProvider: TokenProvider,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    fun refreshAccessToken(refreshToken: String): Tokens {
        val userId =
            tokenProvider.getUserIdFromToken(refreshToken)
                ?: throw InvalidRefreshTokenException(
                    details = mapOf("refreshToken" to refreshToken),
                )
        val savedRefreshToken =
            authRedisRepository.getRefreshToken(userId) ?: throw InvalidRefreshTokenException(
                details = mapOf("userId" to userId, "refreshToken" to "Invalid or already expired"),
            )

        if (refreshToken != savedRefreshToken) {
            authRedisRepository.deleteRefreshToken(userId)
            throw InvalidRefreshTokenException(
                details = mapOf("userId" to userId, "refreshToken" to "Reused"),
            )
        }

        val userSimple =
            userRepository.findByIdAndDeletedAtIsNull(userId)?.let { UserSimple(it.requireId(), it.getRole()) }
                ?: throw UserNotFoundException(
                    details = mapOf("userId" to userId),
                )

        val tokens = tokenProvider.generateTokens(userSimple)
        authRedisRepository.saveRefreshToken(userSimple.id, tokens.refreshToken)
        return tokens
    }

    fun authenticate(accessToken: String): AuthUserPrincipal {
        val userId =
            tokenProvider.getUserIdFromToken(accessToken)
                ?: throw InvalidAccessTokenException()

        return userRepository.findByIdAndDeletedAtIsNull(userId)
            ?.let {
                AuthUserPrincipal(
                    id = it.requireId(),
                    name = it.name,
                    userRole = it.getRole(),
                    email = it.email,
                    passwordHash = it.passwordHash,
                )
            } ?: throw UserNotFoundException(
            details = mapOf("userId" to userId),
        )
    }
}
