package com.waffletoy.team1server.user.service

import com.waffletoy.team1server.user.*
import com.waffletoy.team1server.user.controller.*
import com.waffletoy.team1server.user.persistence.UserEntity
import com.waffletoy.team1server.user.persistence.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val googleOAuth2Client: GoogleOAuth2Client,
    private val emailService: EmailService,
    private val redisTokenService: RedisTokenService,
) {
    // 회원가입
    @Transactional
    fun signUp(
        authProvider: AuthProvider,
        email: String,
        nickname: String?,
        loginId: String?,
        password: String?,
        socialAccessToken: String?,
    ): Pair<User, UserTokenUtil.Tokens> {
        val finalEmail: String
        val finalNickname: String

        if (authProvider == AuthProvider.GOOGLE) {
            // 구글 소셜 로그인
            // 필수값 확인
            if (socialAccessToken.isNullOrBlank()) {
                throw SignUpIllegalArgumentException("Social access token is required for Google signup")
            }

            // Google OAuth2를 통해 이메일과 이름 가져오기
            val googleUserInfo = googleOAuth2Client.getUserInfo(socialAccessToken)
            finalEmail = googleUserInfo.email
            finalNickname = nickname ?: googleUserInfo.name
        } else {
            // 로컬 로그인
            // 필수값 확인
            if (nickname.isNullOrBlank()) {
                throw SignUpIllegalArgumentException("Nickname is required for Local signup")
            }
            if (loginId.isNullOrBlank()) {
                throw SignUpIllegalArgumentException("loginId is required for Local signup")
            }
            if (password.isNullOrBlank()) {
                throw SignUpIllegalArgumentException("password is required for Local signup")
            }

            // loginId 조건 확인
            val loginIdRegex = Regex("^[a-zA-Z][a-zA-Z0-9_-]{4,19}$")
            if (!loginIdRegex.matches(loginId)) {
                throw SignUpBadArgumentException("loginId must be 5-20 characters long and only contain letters, numbers, '_', or '-'")
            }

            // password 조건 확인
            val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$!^*])[A-Za-z\\d@#$!^*]{8,20}$")
            if (!passwordRegex.matches(password)) {
                throw SignUpBadArgumentException("password must be 8-20 characters long, include at least 1 uppercase letter, 1 lowercase letter, 1 number, and 1 special character (@#$!^*)")
            }

            // 이미 같은 로그인Id가 존재한다면 throw(CONFLICT)
            if (userRepository.existsByLoginId(loginId)) {
                throw SignUpConflictException("User LoginID Conflict")
            }

            finalEmail = email
            finalNickname = nickname
        }

        // 이미 이메일이 존재한다면 throw(CONFLICT)
        if (userRepository.existsByEmail(finalEmail)) {
            throw SignUpConflictException("User Email Conflict")
        }

        // 비밀번호 암호화 - 소셜 로그인은 비밀번호 없음
        val encryptedPassword =
            password?.let {
                BCrypt.hashpw(it, BCrypt.gensalt())
            }

        // 유저 정보 저장
        val user =
            userRepository.save(
                UserEntity(
                    email = finalEmail,
                    nickname = finalNickname,
                    status = UserStatus.INACTIVE,
                    authProvider = authProvider,
                    loginId = loginId,
                    password = encryptedPassword,
                ),
            )

        // 토큰 발급 및 저장
        val tokens = issueTokens(user)

        // 인증 이메일 발송
        emailService.sendEmailVerification(user, email)

        return Pair(User.fromEntity(user), tokens)
    }

    // 로그인
    @Transactional
    fun signIn(
        authProvider: AuthProvider,
        socialAccessToken: String?,
        loginId: String?,
        password: String?,
    ): Pair<User, UserTokenUtil.Tokens> {
        val finalUser: UserEntity

        if (authProvider == AuthProvider.GOOGLE) {
            // 구글 소셜 로그인
            // 필수값 확인
            if (socialAccessToken.isNullOrBlank()) {
                throw SignInIllegalArgumentException("Social access token is required for Google signIn")
            }

            // Google OAuth2를 통해 이메일과 이름 가져오기
            val googleUserInfo = googleOAuth2Client.getUserInfo(socialAccessToken)
            val email = googleUserInfo.email

            val user =
                userRepository.findByEmail(email)
                    ?: throw SignInUserNotFoundException()
            finalUser = user
        } else {
            // 로컬 로그인
            if (loginId.isNullOrBlank()) {
                throw SignInIllegalArgumentException("loginId is required for Local signin")
            }
            if (password.isNullOrBlank()) {
                throw SignInIllegalArgumentException("password is required for Local signin")
            }
            val user =
                userRepository.findByLoginId(loginId)
                    ?: throw SignInUserNotFoundException()

            // 비밀번호 확인(소셜 로그인이면 null)
            if (!BCrypt.checkpw(password, user.password)) {
                throw SignInInvalidPasswordException()
            }

            finalUser = user
        }

        // RTR 방식
        // 새로운 Access Token 및 Refresh Token 발급
        val newTokens = UserTokenUtil.generateTokens(finalUser)

        // 새 Refresh Token 저장 - 기존 토큰이 있으면 삭제됨(RTR)
        redisTokenService.saveRefreshToken(finalUser.id, newTokens.refreshToken)

        return Pair(User.fromEntity(finalUser), newTokens)
    }

    // Access Token 만료 시 Refresh Token으로 재발급
    @Transactional
    fun refreshAccessToken(refreshToken: String): UserTokenUtil.Tokens {
        // Refresh Token 유효성 검증 (Redis에서 확인)
        val userId =
            redisTokenService.getUserIdByRefreshToken(refreshToken)
                ?: throw RefreshTokenInvalidException("Invalid Refresh Token")

        // 사용자 정보 조회
        val userEntity =
            userRepository.findByIdOrNull(userId)
                ?: throw RefreshTokenInvalidException("User not found for Refresh Token")

        // 새로운 Access Token 및 Refresh Token 발급
        val newTokens = UserTokenUtil.generateTokens(userEntity)

        // 새 Refresh Token 저장 - 기존 토큰이 있으면 삭제됨(RTR)
        redisTokenService.saveRefreshToken(userId, newTokens.refreshToken)

        return newTokens
    }

    // Access token으로 인증
    @Transactional
    fun authenticate(accessToken: String): User {
        // Access Token 검증 및 사용자 ID 추출
        val userId =
            UserTokenUtil.validateAccessTokenGetUserId(accessToken)
                ?: throw AuthenticateException()

        // 사용자 정보 조회
        val userEntity =
            userRepository.findByIdOrNull(userId)
                ?: throw UserNotFound("User not found in Authenticate")

        return User.fromEntity(userEntity)
    }

    @Transactional
    fun markEmailAsVerified(userId: String) {
        val user =
            userRepository.findByIdOrNull(userId)
                ?: throw UserNotFound("User not found in email verification")

        user.status = UserStatus.ACTIVE
        userRepository.save(user)
    }

    @Transactional
    fun logout(
        refreshToken: String,
        accessToken: String,
    ) {
        // Access Token 검증 및 사용자 ID 추출
        val userId =
            UserTokenUtil.validateAccessTokenGetUserId(accessToken)
                ?: throw AccessTokenInvalidException("Invalid Access Token")

        // Redis에서 Refresh Token 조회
        val storedUserId =
            redisTokenService.getUserIdByRefreshToken(refreshToken)
                ?: throw RefreshTokenInvalidException("Invalid Refresh Token")

        if (userId != storedUserId) {
            throw AccessTokenInvalidException("Access Token and Refresh Token do not match")
        }

        // Refresh Token 삭제
        redisTokenService.deleteRefreshTokenByUserId(userId)
    }

    private fun issueTokens(user: UserEntity): UserTokenUtil.Tokens {
        val tokens = UserTokenUtil.generateTokens(user)
        redisTokenService.saveRefreshToken(user.id, tokens.refreshToken)
        return tokens
    }
}
