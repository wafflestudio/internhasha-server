package com.waffletoy.team1server.user.service

import com.waffletoy.team1server.email.service.EmailService
import com.waffletoy.team1server.user.EmailServiceException
import com.waffletoy.team1server.user.Role
import com.waffletoy.team1server.user.UserServiceException
import com.waffletoy.team1server.user.controller.*
import com.waffletoy.team1server.user.dtos.*
import com.waffletoy.team1server.user.persistence.UserEntity
import com.waffletoy.team1server.user.persistence.UserRepository
import com.waffletoy.team1server.user.utils.UserTokenUtil
import jakarta.transaction.Transactional
import org.mindrot.jbcrypt.BCrypt
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class UserService(
    val userRepository: UserRepository,
    val userRedisCacheService: UserRedisCacheService,
    val googleOAuth2Client: GoogleOAuth2Client,
    val emailService: EmailService,
) {
    // TODO: UserServiceException을 여기서 쓰는 식이 아니라 Exception을 모아서 declare해줘야 ( UserExistsByLocalLoginIdException)
    // Sign up functions
    fun checkDuplicateId(request: CheckDuplicateIdRequest) {
        if (userRepository.existsByLocalLoginId(request.id)) {
            throw UserServiceException(
                "동일한 로컬 아이디로 등록된 사용자가 존재합니다.",
                HttpStatus.CONFLICT,
                1,
            )
        }
    }

    fun checkDuplicateSnuMail(request: CheckDuplicateSnuMailRequest) {
    }

    @Transactional
    fun signUp(request: SignUpRequest): Pair<User, UserTokenUtil.Tokens> {
        val user: User
        when (request.authType) { // TODO: inconsistency 우려 + 이거 그냥 request.info를 넣어서 when is LocalAPplicant로 하면 되는 거 아닌지
            SignUpRequest.AuthType.LOCAL_APPLICANT -> {
                val info = request.info as SignUpRequest.Info.LocalApplicantInfo
                user = localApplicantSignUp(info)
            }

            SignUpRequest.AuthType.SOCIAL_APPLICANT -> {
                val info = request.info as SignUpRequest.Info.SocialApplicantInfo
                user = socialApplicantSignUp(info)
            }

            SignUpRequest.AuthType.POST_ADMIN -> {
                throw UserServiceException() // TODO NOT IMPLEMENTED
            }
        }
        val tokens = UserTokenUtil.generateTokens(user)
        return Pair(user, tokens)
    }

    // TODO verbose control flow
    fun localApplicantSignUp(info: SignUpRequest.Info.LocalApplicantInfo): User {
        var user = userRepository.findBySnuMail(info.snuMail)
        var isMerged = false
        if (user != null) {
            if (user.role != Role.ROLE_APPLICANT) {
                throw UserServiceException() // TODO
            }
            if (user.isLocalLoginImplemented()) {
                throw UserServiceException() // TODO
            }
            if (user.isGoogleLoginImplemented()) {
                user.localLoginId = info.localLoginId
                user.localLoginPasswordHash = BCrypt.hashpw(info.password, BCrypt.gensalt())
                user = userRepository.save(user)
                isMerged = true
            } else {
                throw UserServiceException() // TODO UNKNOWN
            }
        } else {
            user =
                userRepository.save(
                    UserEntity(
                        snuMail = info.snuMail,
                        name = info.name,
                        localLoginId = info.localLoginId,
                        localLoginPasswordHash = BCrypt.hashpw(info.password, BCrypt.gensalt()),
                        role = Role.ROLE_APPLICANT,
                    ),
                )
        }

        return User.fromEntity(entity = user, isMerged = isMerged)
    }

    fun socialApplicantSignUp(info: SignUpRequest.Info.SocialApplicantInfo): User {
        when (info.provider.lowercase()) {
            "google" -> {
                return googleApplicantSignUp(info)
            } // TODO Enum으로 하는 게 나았을듯 적어도 lowercasing이라도
            else -> {
                throw UserServiceException() // TODO
            }
        }
    }

    fun googleApplicantSignUp(info: SignUpRequest.Info.SocialApplicantInfo): User {
        val googleInfo = googleOAuth2Client.getUserInfo(info.token)
        var user = userRepository.findBySnuMail(googleInfo.sub)
        var isMerged = false
        if (user != null) {
            if (user.role != Role.ROLE_APPLICANT) {
                throw UserServiceException() // TODO
            }
            if (user.isGoogleLoginImplemented()) {
                throw UserServiceException() // TODO
            }
            if (user.isLocalLoginImplemented()) {
                user.googleLoginId = googleInfo.sub
                user = userRepository.save(user)
                isMerged = true
            } else {
                throw UserServiceException() // TODO ERROR UNKNOWN
            }
        } else {
            user =
                userRepository.save(
                    UserEntity(
                        snuMail = info.snuMail,
                        name = googleInfo.name,
                        googleLoginId = googleInfo.sub,
                        role = Role.ROLE_APPLICANT,
                    ),
                )
        }
        return User.fromEntity(entity = user, isMerged = isMerged)
    }

    // Signing in and out

    @Transactional
    fun signIn(request: SignInRequest): Pair<User, UserTokenUtil.Tokens> {
        val user: User
        when (request.authType) { // TODO: inconsistency 우려 + 이거 그냥 request.info를 넣어서 when is LocalApplicant로 하면 되는 거 아닌지
            SignInRequest.AuthType.LOCAL -> {
                val info = request.info as SignInRequest.Info.LocalInfo
                user = localSignIn(info)
            }

            SignInRequest.AuthType.SOCIAL -> {
                val info = request.info as SignInRequest.Info.SocialInfo
                user = socialSignIn(info)
            }
        }
        val tokens = UserTokenUtil.generateTokens(user)
        return Pair(user, tokens)
    }

    fun localSignIn(info: SignInRequest.Info.LocalInfo): User {
        val user = userRepository.findByLocalLoginId(info.localLoginId)
        user?.let {
            return User.fromEntity(entity = it)
        } ?: throw UserServiceException()
    }

    fun socialSignIn(info: SignInRequest.Info.SocialInfo): User {
        when (info.provider.lowercase()) {
            "google" -> {
                return googleSignIn(info)
            }
            else -> {
                throw UserServiceException()
            }
        }
    }

    fun googleSignIn(info: SignInRequest.Info.SocialInfo): User {
        val googleInfo = googleOAuth2Client.getUserInfo(info.token)
        val user = userRepository.findByGoogleLoginId(googleInfo.sub)
        user?.let {
            return User.fromEntity(entity = it)
        } ?: throw UserServiceException()
    }

    fun signOut(
        user: User,
        refreshToken: String,
    ) {
        val userId =
            userRedisCacheService.getUserIdByRefreshToken(refreshToken)
                ?: throw UserServiceException(
                    "Invalid refresh token",
                    HttpStatus.BAD_REQUEST,
                ) // TODO
        if (user.id != userId) {
            throw UserServiceException(
                "Access token do not match with refresh token",
                HttpStatus.BAD_REQUEST,
            ) // TODO
        }
    }

    // Token related functions
    // @Transactional(readOnly = true)
    fun authenticate(accessToken: String): User {
        val userId =
            UserTokenUtil.validateAccessTokenGetUserId(accessToken)
                ?: throw UserServiceException() // TODO
        val user =
            userRepository.findByIdOrNull(userId)
                ?: throw UserServiceException() // TODO
        return User.fromEntity(entity = user)
    }

    @Transactional
    fun refreshAccessToken(refreshToken: String): UserTokenUtil.Tokens {
        val userId =
            userRedisCacheService.getUserIdByRefreshToken(refreshToken)
                ?: throw UserServiceException(
                    "유효하지 않은 refresh token(token 조회 실패)",
                    HttpStatus.UNAUTHORIZED,
                )

        // 사용자 정보 조회
        val userEntity =
            userRepository.findByIdOrNull(userId)
                ?: throw UserServiceException(
                    "유효하지 않은 refresh token(userId 조회 실패)",
                    HttpStatus.NOT_FOUND,
                )

        // 토큰 발급 및 저장
        val tokens = UserTokenUtil.generateTokens(User.fromEntity(userEntity))
        return tokens
    }

    // Email verification

    fun fetchGoogleAccountEmail(request: FetchGoogleAccountEmailRequest): String {
        return googleOAuth2Client.getUserInfo(request.accessToken).email
    }

    fun sendSnuMailVerification(request: SendSnuMailVerificationRequest) {
        if (userRepository.existsBySnuMail(request.snuMail)) {
            throw UserServiceException(
                "동일한 스누메일로 등록된 계정이 존재합니다.",
                HttpStatus.CONFLICT,
            )
        } // TODO: 이러면 merge는 어떻게?

        // 이메일 인증 토큰 생성
        val emailCode = (100000..999999).random().toString()
        val encryptedEmailCode = BCrypt.hashpw(emailCode, BCrypt.gensalt())

        // Redis 에 Email Token 저장
        userRedisCacheService.saveEmailCode(request.snuMail, encryptedEmailCode)
        emailService.sendEmail(
            to = request.snuMail,
            subject = "이메일 인증 요청",
            text = "이메일 인증 번호: $emailCode",
        )
    }

    fun checkSnuMailVerification(request: CheckSnuMailVerificationRequest) {
        val encryptedCode =
            userRedisCacheService.getEmailCode(request.snuMail)
                ?: throw EmailServiceException(
                    "이메일로 전달된 코드가 유효하지 않습니다.",
                    HttpStatus.FORBIDDEN,
                )

        // 입력된 인증 코드와 Redis에 저장된 암호화된 코드 비교
        if (!BCrypt.checkpw(request.code, encryptedCode)) {
            throw EmailServiceException(
                "인증 코드와 입력 코드가 일치하지 않습니다.",
                HttpStatus.BAD_REQUEST,
            )
        } else {
            userRedisCacheService.deleteEmailCode(request.snuMail)
        }
    }

    @Value("\${custom.SECRET}")
    private lateinit var resetDbSecret: String

    fun resetDatabase(secret: String) {
        if (secret != resetDbSecret) {
            throw UserServiceException(
                "적절한 Key를 X-Secret에 넣어주세요 ",
                HttpStatus.FORBIDDEN,
            )
        }
        userRepository.deleteAll()
        userRedisCacheService.deleteAll()
    }
}
