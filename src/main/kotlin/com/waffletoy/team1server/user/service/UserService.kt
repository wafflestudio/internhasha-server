package com.waffletoy.team1server.user.service

import com.waffletoy.team1server.user.*
import com.waffletoy.team1server.user.controller.*
import com.waffletoy.team1server.user.persistence.UserEntity
import com.waffletoy.team1server.user.persistence.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
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
        snuMail: String,
        nickname: String?,
        loginId: String?,
        password: String?,
        googleAccessToken: String?,
    ): Pair<User, UserTokenUtil.Tokens> {
        val finalNickname: String
        var googleId: String? = null
        var googleEmail: String? = null

        if (authProvider == AuthProvider.GOOGLE) {
            // 구글 소셜 로그인
            // 필수값 확인
            if (googleAccessToken.isNullOrBlank()) {
                throw UserServiceException(
                    "Social access token is required for Google signup",
                    HttpStatus.BAD_REQUEST,
                )
            }

            // Google OAuth2를 통해 구글 이메일과 이름, 구글 id 가져오기 (실패하면 NOT_FOUND)
            val googleUserInfo = googleOAuth2Client.getUserInfo(googleAccessToken)

            googleEmail = googleUserInfo.email
            finalNickname = nickname ?: googleUserInfo.name
            googleId = googleUserInfo.sub

            // 이미 같은 구글 아이디가 존재한다면 throw(CONFLICT)
            if (userRepository.existsByGoogleId(googleId)) {
                throw UserServiceException(
                    "GoogleId Conflict",
                    HttpStatus.CONFLICT,
                )
            }
        } else {
            // 로컬 로그인
            // 필수값 확인
            if (nickname.isNullOrBlank()) {
                throw UserServiceException(
                    "Nickname is required for Local signup",
                    HttpStatus.BAD_REQUEST,
                )
            }
            if (loginId.isNullOrBlank()) {
                throw UserServiceException(
                    "loginId is required for Local signup",
                    HttpStatus.BAD_REQUEST,
                )
            }
            if (password.isNullOrBlank()) {
                throw UserServiceException(
                    "password is required for Local signup",
                    HttpStatus.BAD_REQUEST,
                )
            }

            // loginId 조건 확인
            val loginIdRegex = Regex("^[a-zA-Z][a-zA-Z0-9_-]{4,19}$")
            if (!loginIdRegex.matches(loginId)) {
                throw UserServiceException(
                    "loginId must be 5-20 characters long and only contain letters, numbers, '_', or '-'",
                    HttpStatus.BAD_REQUEST,
                )
            }

            // password 조건 확인
            val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$!^*])[A-Za-z\\d@#$!^*]{8,20}$")
            if (!passwordRegex.matches(password)) {
                throw UserServiceException(
                    "password must be 8-20 characters long, include at least 1 uppercase letter, 1 lowercase letter, 1 number, and 1 special character (@#$!^*)",
                    HttpStatus.BAD_REQUEST,
                )
            }

            // 이미 같은 로그인Id가 존재한다면 throw(CONFLICT)
            if (userRepository.existsByLoginId(loginId)) {
                throw UserServiceException(
                    "User LoginID Conflict",
                    HttpStatus.CONFLICT,
                )
            }
            finalNickname = nickname
        }

        // 이미 스누메일이 존재한다면 throw(CONFLICT)
        if (userRepository.existsBySnuMail(snuMail)) {
            throw UserServiceException(
                "User SnuMail Conflict",
                HttpStatus.CONFLICT,
            )
        }

        // 스누메일이 아니면 throw
        if (!snuMail.endsWith("@snu.ac.kr")) {
            throw UserServiceException(
                "Requested mail is not SNU Mail",
                HttpStatus.BAD_REQUEST,
            )
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
                    snuMail = snuMail,
                    nickname = finalNickname,
                    status = UserStatus.INACTIVE,
                    authProvider = authProvider,
                    loginId = loginId,
                    password = encryptedPassword,
                    googleId = googleId,
                    googleEmail = googleEmail,
                ),
            )

        // 토큰 발급 및 저장
        val tokens = issueTokens(user)

        // 인증 이메일 발송
        emailService.sendEmailVerification(user, snuMail)

        return Pair(User.fromEntity(user), tokens)
    }

    // 로그인
    @Transactional
    fun signIn(
        authProvider: AuthProvider,
        googleAccessToken: String?,
        loginId: String?,
        password: String?,
    ): Pair<User, UserTokenUtil.Tokens> {
        val finalUser: UserEntity

        if (authProvider == AuthProvider.GOOGLE) {
            // 구글 소셜 로그인
            // 필수값 확인
            if (googleAccessToken.isNullOrBlank()) {
                throw UserServiceException(
                    "Social access token is required for Google signIn",
                    HttpStatus.BAD_REQUEST,
                )
            }

            // Google OAuth2를 통해 이메일과 이름 가져오기
            val googleUserInfo = googleOAuth2Client.getUserInfo(googleAccessToken)
            val googleEmail = googleUserInfo.email
            val googleId = googleUserInfo.sub

            val user =
                userRepository.findByGoogleId(googleId)
                    ?: throw UserServiceException(
                        "User Not Found",
                        HttpStatus.NOT_FOUND,
                    )

            // Google 이메일 확인
            if (user.googleEmail != googleEmail) {
                throw UserServiceException(
                    "The provided Google ID does not match the user's record.",
                    HttpStatus.BAD_REQUEST,
                )
            }

            finalUser = user
        } else {
            // 로컬 로그인
            if (loginId.isNullOrBlank()) {
                throw UserServiceException(
                    "loginId is required for Local sign in",
                    HttpStatus.BAD_REQUEST,
                )
            }
            if (password.isNullOrBlank()) {
                throw UserServiceException(
                    "password is required for Local sign in",
                    HttpStatus.BAD_REQUEST,
                )
            }
            val user =
                userRepository.findByLoginId(loginId)
                    ?: throw UserServiceException(
                        "User Not Found",
                        HttpStatus.NOT_FOUND,
                    )

            // 비밀번호 확인(소셜 로그인이면 null)
            if (!BCrypt.checkpw(password, user.password)) {
                throw UserServiceException(
                    "The provided password does not match the user's record.",
                    HttpStatus.BAD_REQUEST,
                )
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
                ?: throw UserServiceException(
                    "Invalid Refresh Token",
                    HttpStatus.BAD_REQUEST,
                )

        // 사용자 정보 조회
        val userEntity =
            userRepository.findByIdOrNull(userId)
                ?: throw UserServiceException(
                    "User not found for Refresh Token",
                    HttpStatus.NOT_FOUND,
                )

        // 새로운 Access Token 및 Refresh Token 발급
        val newTokens = UserTokenUtil.generateTokens(userEntity)

        // 새 Refresh Token 저장 - 기존 토큰이 있으면 삭제됨(RTR)
        redisTokenService.saveRefreshToken(userId, newTokens.refreshToken)

        return newTokens
    }

    @Transactional
    fun markEmailAsVerified(userId: String) {
        val user =
            userRepository.findByIdOrNull(userId)
                ?: throw UserServiceException(
                    "User not found in email verification",
                    HttpStatus.NOT_FOUND,
                )

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
            authAccessToken(accessToken).id

        // Redis에서 Refresh Token 조회
        val storedUserId =
            redisTokenService.getUserIdByRefreshToken(refreshToken)
                ?: throw UserServiceException(
                    "Invalid Refresh Token",
                    HttpStatus.BAD_REQUEST,
                )

        if (userId != storedUserId) {
            throw UserServiceException(
                "Access Token do not match with Refresh Token",
                HttpStatus.BAD_REQUEST,
            )
        }

        // Refresh Token 삭제
        redisTokenService.deleteRefreshTokenByUserId(userId)
    }

    // Access token으로 인증
    @Transactional
    public fun authAccessToken(accessToken: String): UserEntity {
        // Access Token 검증 및 사용자 ID 추출
        val userId =
            UserTokenUtil.validateAccessTokenGetUserId(accessToken)
                ?: throw UserServiceException(
                    "Access Token Validation Failed",
                    HttpStatus.UNAUTHORIZED,
                )

        // 사용자 정보 조회
        val userEntity =
            userRepository.findByIdOrNull(userId)
                ?: throw UserServiceException(
                    "User not found Searched by Access Token",
                    HttpStatus.NOT_FOUND,
                )

        return userEntity
    }

    @Transactional
    fun changePassword(
        accessToken: String,
        oldPassword: String,
        newPassword: String,
    ) {
        val user = authAccessToken(accessToken)

        // 비밀번호 확인(소셜 로그인이면 null)
        if (!BCrypt.checkpw(oldPassword, user.password)) {
            throw UserServiceException(
                "The provided password does not match the user's record.",
                HttpStatus.BAD_REQUEST,
            )
        }

        user.password = oldPassword
        userRepository.save(user)
    }

    private fun issueTokens(user: UserEntity): UserTokenUtil.Tokens {
        val tokens = UserTokenUtil.generateTokens(user)
        redisTokenService.saveRefreshToken(user.id, tokens.refreshToken)
        return tokens
    }
}
