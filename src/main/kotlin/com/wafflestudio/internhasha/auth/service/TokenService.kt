package com.wafflestudio.internhasha.auth.service

import com.wafflestudio.internhasha.auth.exception.UserNotFoundException
import com.wafflestudio.internhasha.auth.repository.AuthRedisRepository
import com.wafflestudio.internhasha.auth.repository.UserRepository
import com.wafflestudio.internhasha.auth.security.AuthUserPrincipal
import com.wafflestudio.internhasha.auth.security.TokenProvider
import com.wafflestudio.internhasha.auth.service.dto.Tokens
import com.wafflestudio.internhasha.auth.service.dto.UserSimple
import com.wafflestudio.internhasha.common.event.UserDeletedEvent
import com.wafflestudio.internhasha.exceptions.InvalidAccessTokenException
import com.wafflestudio.internhasha.exceptions.InvalidRefreshTokenException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Service
@Transactional(readOnly = true)
class TokenService(
    private val userRepository: UserRepository,
    private val authRedisRepository: AuthRedisRepository,
    private val tokenProvider: TokenProvider,
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

        return userRepository
            .findByIdAndDeletedAtIsNull(userId)
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

    // 유저 탈퇴 시의 캐시 삭제는 트랜잭션 종료 후 호출(동기)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleUserDeletedEvent(event: UserDeletedEvent) {
        authRedisRepository.deleteRefreshToken(event.userId)
        authRedisRepository.deleteEmailCode(event.userId)
        authRedisRepository.deleteSuccessCode(event.userId)
    }
}
