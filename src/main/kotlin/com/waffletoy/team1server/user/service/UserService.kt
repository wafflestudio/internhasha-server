package com.waffletoy.team1server.user.service

import com.waffletoy.team1server.user.*
import com.waffletoy.team1server.user.controller.User
import com.waffletoy.team1server.user.persistence.UserEntity
import com.waffletoy.team1server.user.persistence.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    // 회원가입
    @Transactional
    fun signUp(
        name: String,
        email: String,
        phoneNumber: String,
        password: String?,
        authProvider: AuthProvider,
    ): User {
        // 이미 이메일이 존재한다면 throw
        if (userRepository.existsByEmail(email)) {
            throw SignUpUserEmailConflictException()
        }

        // 비밀번호 암호화 (null이면 그대로) - 소셜 로그인은 비밀번호 없음
        val encryptedPassword =
            password?.let {
                BCrypt.hashpw(it, BCrypt.gensalt())
            }

        // 유저 정보 저장
        val user =
            userRepository.save(
                UserEntity(
                    name = name,
                    email = email,
                    phoneNumber = phoneNumber,
                    password = encryptedPassword,
                    status = UserStatus.INACTIVE,
                    authProvider = authProvider,
                    authoredPosts = emptySet(),
                ),
            )

        return User.fromEntity(user)
    }

    // 로그인
    @Transactional
    fun signIn(
        email: String,
        password: String,
        authProvider: AuthProvider,
    ): Pair<User, UserTokenUtil.Tokens> {
        // 입력한 이메일을 기준으로 해당 유저를 찾음
        val targetUser = userRepository.findByEmail(email) ?: throw SignInUserNotFoundException()

        // 비밀번호 확인(소셜 로그인이면 null)
        if (!BCrypt.checkpw(password, targetUser.password)) {
            throw SignInInvalidPasswordException()
        }

        // 토큰 발급
        val tokens = UserTokenUtil.generateTokens(targetUser, userRepository)

        return Pair(User.fromEntity(targetUser), tokens)
    }

    // Access Token 만료 시 Refresh Token으로 재발급
    @Transactional
    fun refreshAccessToken(refreshToken: String): UserTokenUtil.Tokens {
        val now = Instant.now()

        // Refresh Token 유효성 검증
        val userEntity =
            userRepository.findByRefreshToken(refreshToken)
                ?: throw RefreshTokenInvalidException()

        if (userEntity.refreshTokenExpiresAt?.isBefore(now) == true) {
            throw RefreshTokenExpiredException()
        }

        // 새 토큰 발급
        return UserTokenUtil.generateTokens(userEntity, userRepository)
    }

    // Access token으로 인증
    @Transactional
    fun authenticate(accessToken: String): User {
        val userId = UserTokenUtil.validateAccessTokenGetUserId(accessToken) ?: throw AuthenticateException()
        val user = userRepository.findByIdOrNull(userId.toInt()) ?: throw AuthenticateException()
        return User.fromEntity(user)
    }
}
