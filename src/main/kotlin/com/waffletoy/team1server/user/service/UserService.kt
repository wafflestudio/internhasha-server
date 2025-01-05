package com.waffletoy.team1server.user.service

import com.waffletoy.team1server.user.*
import com.waffletoy.team1server.user.controller.*
import com.waffletoy.team1server.user.persistence.UserEntity
import com.waffletoy.team1server.user.persistence.UserRepository
import org.mindrot.jbcrypt.BCrypt
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
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
        snuMail: String,
        username: String? = null,
        localId: String? = null,
        password: String? = null,
        googleAccessToken: String? = null,
    ): Pair<User, UserTokenUtil.Tokens> {
        val finalUsername: String
        var googleId: String? = null
        var googleEmail: String? = null

        // 이미 등록된 스누 메일인지 확인
        if (userRepository.existsBySnuMail(snuMail)) {
            throw EmailServiceException(
                "동일한 스누메일로 등록된 계정이 존재합니다.",
                HttpStatus.CONFLICT,
            )
        }

        // 스누메일이 아니면 throw
        if (!snuMail.endsWith("@snu.ac.kr")) {
            throw UserServiceException(
                "스누메일 형식에 맞지 않습니다.",
                HttpStatus.BAD_REQUEST,
            )
        }

        if (googleAccessToken!=null) {
            // 구글 소셜 로그인
            // 필수값 확인
            if (googleAccessToken.isBlank()) {
                throw UserServiceException(
                    "구글 엑세스 토큰 필드가 비어있습니다.",
                    HttpStatus.BAD_REQUEST,
                )
            }

            // Google OAuth2를 통해 구글 이메일과 이름, 구글 id 가져오기
            // 가져오는 데 실패하면(토큰이 유효하지 않거나 통신 실패)
            // 400 Bad Request
            val googleUserInfo = googleOAuth2Client.getUserInfo(googleAccessToken)

            googleEmail = googleUserInfo.email
            finalUsername = googleUserInfo.name
            googleId = googleUserInfo.sub

            // 이미 같은 구글 아이디가 존재한다면 throw(CONFLICT)
            if (userRepository.existsByGoogleId(googleId)) {
                throw UserServiceException(
                    "동일한 구글 계정으로 등록된 계정이 존재합니다.",
                    HttpStatus.CONFLICT,
                )
            }
        } else {
            // 로컬 로그인
            // 필수값 확인
            if (username.isNullOrBlank()) {
                throw UserServiceException(
                    "Username is required for Local signup",
                    HttpStatus.BAD_REQUEST,
                )
            }
            if (localId.isNullOrBlank()) {
                throw UserServiceException(
                    "localId is required for Local signup",
                    HttpStatus.BAD_REQUEST,
                )
            }
            if (password.isNullOrBlank()) {
                throw UserServiceException(
                    "password is required for Local signup",
                    HttpStatus.BAD_REQUEST,
                )
            }

            // localId 조건 확인
            if (!isValidlocalId(localId)) {
                throw UserServiceException(
                    "localId must be 5-20 characters long and only contain letters, numbers, '_', or '-'",
                    HttpStatus.BAD_REQUEST,
                )
            }

            // password 조건 확인
            if (!isValidPassword(password)) {
                throw UserServiceException(
                    "password must be 8-20 characters long, include at least 1 uppercase letter, 1 lowercase letter, 1 number, and 1 special character (@#$!^*)",
                    HttpStatus.BAD_REQUEST,
                )
            }

            // 이미 같은 로그인Id가 존재한다면 throw(CONFLICT)
            if (userRepository.existsByLocalId(localId)) {
                throw UserServiceException(
                    "동일한 아이디로 등록된 계정이 존재합니다",
                    HttpStatus.CONFLICT,
                )
            }
            finalUsername = username
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
                    username = finalUsername,
                    localId = localId,
                    password = encryptedPassword,
                    googleId = googleId,
                ),
            )

        // 토큰 발급 및 저장
        val tokens = issueTokens(user)
        return Pair(User.fromEntity(user), tokens)
    }

    // 로그인
    @Transactional
    fun signIn(
        googleAccessToken: String? = null,
        localId: String? = null,
        password: String? = null,
    ): Pair<User, UserTokenUtil.Tokens> {
        val finalUser: UserEntity

        if (googleAccessToken != null) {
            // 필수값 확인
            if (googleAccessToken.isBlank()) {
                throw UserServiceException(
                    "구글 엑세스 토큰 필드가 비어있습니다.",
                    HttpStatus.BAD_REQUEST,
                )
            }

            // Google OAuth2를 통해 이메일과 이름 가져오기
            val googleUserInfo = googleOAuth2Client.getUserInfo(googleAccessToken)
            val googleId = googleUserInfo.sub

            val user =
                userRepository.findByGoogleId(googleId)
                    ?: throw UserServiceException(
                        "해당 구글 계정의 사용자 정보가 존재하지 않습니다.",
                        HttpStatus.NOT_FOUND,
                    )
            finalUser = user
            
        } else {
            // 로컬 로그인
            if (localId.isNullOrBlank()) {
                throw UserServiceException(
                    "localId is required for Local sign in",
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
                userRepository.findByLocalId(localId)
                    ?: throw UserServiceException(
                        "해당 아이디의 사용자 정보가 존재하지 않습니다.",
                        HttpStatus.NOT_FOUND,
                    )

            // 비밀번호 확인(소셜 로그인이면 null)
            if (!BCrypt.checkpw(password, user.password)) {
                throw UserServiceException(
                    "비밀번호가 일치하지 않습니다.",
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
        // Refresh Token으로 사용자 ID 조회
        val userId =
            redisTokenService.getUserIdByRefreshToken(refreshToken)
                ?: throw UserServiceException(
                    "유효하지 않은 refresh token(token 조회 실패)",
                    HttpStatus.BAD_REQUEST)

        // 사용자 정보 조회
        val userEntity =
            userRepository.findByIdOrNull(userId)
                ?: throw UserServiceException(
                    "유효하지 않은 refresh token(userId 조회 실패)",
                    HttpStatus.BAD_REQUEST)

        // 새로운 토큰 발급
        val newTokens = UserTokenUtil.generateTokens(userEntity)

        // Redis에 새 Refresh Token 저장
        redisTokenService.saveRefreshToken(userId, newTokens.refreshToken)

        return newTokens
    }

    @Transactional
    fun changePassword(
        user: User,
        oldPassword: String,
        newPassword: String,
    ) {
        val userFromDB =
            userRepository.findByIdOrNull(user.id)
                ?: throw UserServiceException(
                    "유저를 찾지 못했습니다.",
                    HttpStatus.BAD_REQUEST,
                )

        // 소셜 로그인이면 비밀번호를 바꾸지 못 함
        if (userFromDB.password==null) {
            throw UserServiceException(
                "소셜 로그인 회원은 비밀번호 변경이 불가능합니다.",
                HttpStatus.BAD_REQUEST,
            )
        }

        // 비밀번호 확인 (소셜 로그인이면 null)
        if (!BCrypt.checkpw(oldPassword, userFromDB.password)) {
            throw UserServiceException(
                "이전 비밀번호가 일치하지 않습니다.",
                HttpStatus.BAD_REQUEST,
            )
        }

        // password 조건 확인
        if (!isValidPassword(newPassword)) {
            throw UserServiceException(
                "password must be 8-20 characters long, include at least 1 uppercase letter, 1 lowercase letter, 1 number, and 1 special character (@#$!^*)",
                HttpStatus.BAD_REQUEST,
            )
        }

        userFromDB.password = BCrypt.hashpw(newPassword, BCrypt.gensalt())
        userRepository.save(userFromDB)
    }


    @Transactional
    fun logout(
        user: User,
        refreshToken: String,
    ) {
        // Redis에서 Refresh Token 조회
        val storedUserId =
            redisTokenService.getUserIdByRefreshToken(refreshToken)
                ?: throw UserServiceException(
                    "Invalid Refresh Token",
                    HttpStatus.BAD_REQUEST,
                )

        if (user.id != storedUserId) {
            throw UserServiceException(
                "Access Token do not match with Refresh Token",
                HttpStatus.BAD_REQUEST,
            )
        }

        // Refresh Token 삭제
        try {
            redisTokenService.deleteRefreshTokenByUserId(storedUserId)
        } catch (e: Exception) {
            // Refresh Token이 없더라도 로그아웃은 성공으로 간주
        }
    }

    // Access token으로 인증
    @Transactional(readOnly = true)
    fun authenticate(accessToken: String): User {
        val userId =
            UserTokenUtil.validateAccessTokenGetUserId(accessToken)
                ?: throw AuthenticateException("Invalid or expired access token")

        val userEntity =
            userRepository.findByIdOrNull(userId)
                ?: throw AuthenticateException("User not found for the given token")

        return User.fromEntity(userEntity)
    }


    fun deleteAllUsers() {
        userRepository.deleteAll()
        redisTokenService.deleteAllKeys()
    }

    private fun issueTokens(user: UserEntity): UserTokenUtil.Tokens {
        val tokens = UserTokenUtil.generateTokens(user)
        redisTokenService.saveRefreshToken(user.id, tokens.refreshToken)
        return tokens
    }

    private val localIdRegex = Regex("^[a-zA-Z][a-zA-Z0-9_-]{4,19}$")
    private val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$!^*])[A-Za-z\\d@#$!^*]{8,20}$")

    fun isValidlocalId(localId: String): Boolean = localIdRegex.matches(localId)

    fun isValidPassword(password: String): Boolean = passwordRegex.matches(password)
}
