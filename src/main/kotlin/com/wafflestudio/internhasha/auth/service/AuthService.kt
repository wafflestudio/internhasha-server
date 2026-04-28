package com.wafflestudio.internhasha.auth.service

import com.wafflestudio.internhasha.auth.repository.AuthRedisRepository
import com.wafflestudio.internhasha.auth.repository.UserRepository
import com.wafflestudio.internhasha.auth.security.TokenProvider
import com.wafflestudio.internhasha.auth.service.dto.SignInCommand
import com.wafflestudio.internhasha.auth.service.dto.UserSimple
import com.wafflestudio.internhasha.auth.service.dto.UserWithTokens
import com.wafflestudio.internhasha.exceptions.*
import org.mindrot.jbcrypt.BCrypt
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AuthService(
    private val userRepository: UserRepository,
    private val authRedisRepository: AuthRedisRepository,
    private val tokenProvider: TokenProvider,
) {
    // 로그인
    fun signIn(command: SignInCommand): UserWithTokens {
        val user =
            userRepository.findByEmailAndDeletedAtIsNull(command.email)
                ?: throw InvalidCredentialsException()
        if (!BCrypt.checkpw(command.password, user.passwordHash)) {
            throw InvalidCredentialsException()
        }

        // refresh token을 redis에 저장
        // 기존 refresh token 삭제는 생략 - 다중 디바이스 로그인 지원
        val userSimple = UserSimple(user.requireId(), user.getRole())
        val tokens = tokenProvider.generateTokens(userSimple)
        authRedisRepository.saveRefreshToken(userSimple.id, tokens.refreshToken)

        return UserWithTokens(userSimple, tokens)
    }

    fun signOut(
        userId: String,
    ) {
        // 로그아웃 시 Refresh Token 삭제 (Access Token 은 클라이언트 측에서 삭제)
        val result = authRedisRepository.deleteRefreshToken(userId)
        if (!result) {
            throw InvalidRefreshTokenException(
                details = mapOf("userId" to userId, "refreshToken" to "Invalid or already expired"),
            )
        }

        // 추후 유저의 Access Token 을 Access Token 의 남은 유효시간 만큼
        // Redis 블랙리스트에 추가할 필요성 있음
    }
}
