package com.waffletoy.team1server.user.service

import com.waffletoy.team1server.user.*
import com.waffletoy.team1server.user.UserTokenUtil.isRefreshTokenExpired
import com.waffletoy.team1server.user.controller.*
import com.waffletoy.team1server.user.persistence.UserEntity
import com.waffletoy.team1server.user.persistence.UserRepository
import io.github.cdimascio.dotenv.Dotenv
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
) {
    // 회원가입
    @Transactional
    fun signUp(
        authProvider: AuthProvider,
        email: String,
        nickname: String?,
        loginID: String?,
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
            if (loginID.isNullOrBlank()) {
                throw SignUpIllegalArgumentException("loginID is required for Local signup")
            }
            if (password.isNullOrBlank()) {
                throw SignUpIllegalArgumentException("password is required for Local signup")
            }

            // loginID 조건 확인
            val loginIdRegex = Regex("^[a-zA-Z][a-zA-Z0-9_-]{4,19}$")
            if (!loginIdRegex.matches(loginID)) {
                throw SignUpBadArgumentException("loginID must be 5-20 characters long and only contain letters, numbers, '_', or '-'")
            }

            // password 조건 확인
            val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$!^*])[A-Za-z\\d@#$!^*]{8,20}$")
            if (!passwordRegex.matches(password)) {
                throw SignUpBadArgumentException("password must be 8-20 characters long, include at least 1 uppercase letter, 1 lowercase letter, 1 number, and 1 special character (@#$!^*)")
            }

            // 이미 같은 로그인이 존재한다면 throw(CONFLICT)
            if (userRepository.existsByLoginID(loginID)) {
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
                    loginID = loginID,
                    password = encryptedPassword,
                ),
            )

        // 토큰 발급
        val tokens = UserTokenUtil.generateTokens(user, userRepository)

        // 이메일 전송
        // 이메일 인증 토큰 생성
        val emailToken = UUID.randomUUID().toString()
        user.emailToken = emailToken

        // 이메일 인증 링크 생성
        val verifyLink = "https://$domainURL/verify-email?token=$emailToken"
        // 이메일 발송
        emailService.sendEmail(
            to = finalEmail,
            subject = "이메일 인증 요청",
            body = "이메일 인증 링크: $verifyLink",
        )

        return Pair(User.fromEntity(user), tokens)
    }

    // 로그인
    @Transactional
    fun signIn(
        authProvider: AuthProvider,
        socialAccessToken: String?,
        loginID: String?,
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
            if (loginID.isNullOrBlank()) {
                throw SignInIllegalArgumentException("loginID is required for Local signin")
            }
            if (password.isNullOrBlank()) {
                throw SignInIllegalArgumentException("password is required for Local signin")
            }
            val user =
                userRepository.findByLoginID(loginID)
                    ?: throw SignInUserNotFoundException()

            // 비밀번호 확인(소셜 로그인이면 null)
            if (!BCrypt.checkpw(password, user.password)) {
                throw SignInInvalidPasswordException()
            }

            finalUser = user
        }

        // 토큰 발급
        val tokens = UserTokenUtil.generateTokens(finalUser, userRepository)

        return Pair(User.fromEntity(finalUser), tokens)
    }

    // Access Token 만료 시 Refresh Token으로 재발급
    @Transactional
    fun refreshAccessToken(refreshToken: String): UserTokenUtil.Tokens {
        // Refresh Token 유효성 검증
        val userEntity =
            userRepository.findByRefreshToken(refreshToken)
                ?: throw RefreshTokenInvalidException()

        if (isRefreshTokenExpired(refreshToken)) {
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

    private val dotenv = Dotenv.load()
    private val domainURL =
        dotenv["DOMAIN_URL"]
            ?: System.getenv("DOMAIN_URL")
            ?: throw RuntimeException("DOMAIN_URL not found")
}
