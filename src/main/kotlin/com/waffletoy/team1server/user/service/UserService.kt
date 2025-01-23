package com.waffletoy.team1server.user.service

import com.waffletoy.team1server.email.service.EmailService
import com.waffletoy.team1server.exceptions.*
import com.waffletoy.team1server.user.*
import com.waffletoy.team1server.user.controller.*
import com.waffletoy.team1server.user.dtos.*
import com.waffletoy.team1server.user.persistence.UserEntity
import com.waffletoy.team1server.user.persistence.UserRepository
import com.waffletoy.team1server.user.utils.UserTokenUtil
import jakarta.transaction.Transactional
import org.mindrot.jbcrypt.BCrypt
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userRedisCacheService: UserRedisCacheService,
    private val googleOAuth2Client: GoogleOAuth2Client,
    private val emailService: EmailService,
) {
    // Sign up functions
    fun checkDuplicateId(request: CheckDuplicateIdRequest) {
        if (userRepository.existsByLocalLoginId(request.id)) {
            throw UserDuplicateLocalIdException(
                details = mapOf("localLoginId" to request.id),
            )
        }
    }

    fun checkDuplicateSnuMail(request: CheckDuplicateSnuMailRequest) {
        if (userRepository.existsBySnuMail(request.snuMail)) {
            throw UserDuplicateSnuMailException(
                details = mapOf("snuMail" to request.snuMail),
            )
        }
    }

    @Transactional
    fun signUp(request: SignUpRequest): Pair<User, UserTokenUtil.Tokens> {
        val user: User =
            when (request.authType) {
                SignUpRequest.AuthType.LOCAL_NORMAL -> {
                    val info = request.info as SignUpRequest.Info.LocalNormalInfo
                    localNormalSignUp(info)
                }

                SignUpRequest.AuthType.SOCIAL_NORMAL -> {
                    val info = request.info as SignUpRequest.Info.SocialNormalInfo
                    socialNormalSignUp(info)
                }

                SignUpRequest.AuthType.LOCAL_CURATOR -> {
                    throw NotImplementedException()
                }
            }
        val tokens = UserTokenUtil.generateTokens(user)
        return Pair(user, tokens)
    }

    private fun localNormalSignUp(info: SignUpRequest.Info.LocalNormalInfo): User {
        var user = userRepository.findBySnuMail(info.snuMail)
        var isMerged = false
        if (user != null) {
            if (user.userRole != UserRole.NORMAL) {
                throw UserRoleConflictException(
                    details = mapOf("userId" to user.id, "userRole" to user.userRole),
                )
            }
            if (user.isLocalLoginImplemented()) {
                throw UserDuplicateSnuMailException(
                    details = mapOf("snuMail" to info.snuMail),
                )
            }
            if (user.isGoogleLoginImplemented()) {
                user.localLoginId = info.localLoginId
                user.localLoginPasswordHash = BCrypt.hashpw(info.password, BCrypt.gensalt())
                user = userRepository.save(user)
                isMerged = true
            } else {
                throw UserMergeUnknownFailureException(
                    details = mapOf("userId" to user.id),
                )
            }
        } else {
            if (userRepository.existsByLocalLoginId(info.localLoginId)) {
                throw UserDuplicateLocalIdException(
                    details = mapOf("localLoginId" to info.localLoginId),
                )
            }
            user =
                userRepository.save(
                    UserEntity(
                        snuMail = info.snuMail,
                        name = info.name,
                        localLoginId = info.localLoginId,
                        localLoginPasswordHash = BCrypt.hashpw(info.password, BCrypt.gensalt()),
                        userRole = UserRole.NORMAL,
                    ),
                )
        }

        return User.fromEntity(entity = user, isMerged = isMerged)
    }

    private fun socialNormalSignUp(info: SignUpRequest.Info.SocialNormalInfo): User {
        return when (info.provider.lowercase()) {
            "google" -> googleNormalSignUp(info)
            else -> throw InvalidRequestException(
                details = mapOf("provider" to info.provider),
            )
        }
    }

    private fun googleNormalSignUp(info: SignUpRequest.Info.SocialNormalInfo): User {
        val googleInfo = googleOAuth2Client.getUserInfo(info.token)
        var user = userRepository.findBySnuMail(googleInfo.email)
        var isMerged = false
        if (user != null) {
            if (user.userRole != UserRole.NORMAL) {
                throw UserRoleConflictException(
                    details = mapOf("userId" to user.id, "userRole" to user.userRole),
                )
            }
            if (user.isGoogleLoginImplemented()) {
                throw UserDuplicateGoogleIdException(
                    details = mapOf("googleLoginId" to googleInfo.sub),
                )
            }
            if (user.isLocalLoginImplemented()) {
                user.googleLoginId = googleInfo.sub
                user = userRepository.save(user)
                isMerged = true
            } else {
                throw UserMergeUnknownFailureException(
                    details = mapOf("userId" to user.id),
                )
            }
        } else {
            if (userRepository.existsByGoogleLoginId(googleInfo.sub)) {
                throw UserDuplicateGoogleIdException(
                    details = mapOf("googleLoginId" to googleInfo.sub),
                )
            }
            user =
                userRepository.save(
                    UserEntity(
                        snuMail = info.snuMail,
                        name = googleInfo.name,
                        googleLoginId = googleInfo.sub,
                        userRole = UserRole.NORMAL,
                    ),
                )
        }
        return User.fromEntity(entity = user, isMerged = isMerged)
    }

    // Signing in and out

    @Transactional
    fun signIn(request: SignInRequest): Pair<User, UserTokenUtil.Tokens> {
        val user: User =
            when (request.authType) {
                SignInRequest.AuthType.LOCAL -> {
                    val info = request.info as SignInRequest.Info.LocalInfo
                    localSignIn(info)
                }

                SignInRequest.AuthType.SOCIAL -> {
                    val info = request.info as SignInRequest.Info.SocialInfo
                    socialSignIn(info)
                }
            }
        val tokens = UserTokenUtil.generateTokens(user)
        return Pair(user, tokens)
    }

    private fun localSignIn(info: SignInRequest.Info.LocalInfo): User {
        val user =
            userRepository.findByLocalLoginId(info.localLoginId)
                ?: throw UserNotFoundException(
                    details = mapOf("localLoginId" to info.localLoginId),
                )

        if (!BCrypt.checkpw(info.password, user.localLoginPasswordHash)) {
            throw InvalidCredentialsException(
                details = mapOf("localLoginId" to info.localLoginId),
            )
        }

        return User.fromEntity(entity = user)
    }

    private fun socialSignIn(info: SignInRequest.Info.SocialInfo): User {
        return when (info.provider.lowercase()) {
            "google" -> googleSignIn(info)
            else -> throw InvalidRequestException(
                details = mapOf("provider" to info.provider),
            )
        }
    }

    private fun googleSignIn(info: SignInRequest.Info.SocialInfo): User {
        val googleInfo = googleOAuth2Client.getUserInfo(info.token)
        val user =
            userRepository.findByGoogleLoginId(googleInfo.sub)
                ?: throw UserNotFoundException(
                    details = mapOf("googleLoginId" to googleInfo.sub),
                )
        return User.fromEntity(entity = user)
    }

    fun signOut(
        user: User,
        refreshToken: String,
    ) {
        val userId =
            userRedisCacheService.getUserIdByRefreshToken(refreshToken)
                ?: throw InvalidRefreshTokenException(
                    details = mapOf("refreshToken" to refreshToken),
                )
        if (user.id != userId) {
            throw TokenMismatchException(
                details = mapOf("userId" to user.id, "refreshTokenUserId" to userId),
            )
        }
        // Additional sign-out logic if necessary
    }

    // Token related functions

    fun authenticate(accessToken: String): User {
        val userId =
            UserTokenUtil.validateAccessTokenGetUserId(accessToken)
                ?: throw InvalidAccessTokenException()

        val user =
            userRepository.findByIdOrNull(userId)
                ?: throw UserNotFoundException(
                    details = mapOf("userId" to userId),
                )
        return User.fromEntity(entity = user)
    }

    @Transactional
    fun refreshAccessToken(refreshToken: String): UserTokenUtil.Tokens {
        val userId =
            userRedisCacheService.getUserIdByRefreshToken(refreshToken)
                ?: throw InvalidRefreshTokenException(
                    details = mapOf("refreshToken" to refreshToken),
                )

        val userEntity =
            userRepository.findByIdOrNull(userId)
                ?: throw UserNotFoundException(
                    details = mapOf("userId" to userId),
                )

        return UserTokenUtil.generateTokens(User.fromEntity(entity = userEntity))
    }

    // Email verification

    fun fetchGoogleAccountEmail(request: FetchGoogleAccountEmailRequest): String {
        return googleOAuth2Client.getUserInfo(request.accessToken).email
    }

    fun sendSnuMailVerification(request: SendSnuMailVerificationRequest) {
        if (userRepository.existsBySnuMail(request.snuMail)) {
            throw UserDuplicateSnuMailException(
                details = mapOf("snuMail" to request.snuMail),
            )
        }

        val emailCode = (100000..999999).random().toString()
        val encryptedEmailCode = BCrypt.hashpw(emailCode, BCrypt.gensalt())

        userRedisCacheService.saveEmailCode(request.snuMail, encryptedEmailCode)
        try {
            emailService.sendEmail(
                to = request.snuMail,
                subject = "이메일 인증 요청",
                text = "이메일 인증 번호: $emailCode",
            )
        } catch (ex: Exception) {
            throw EmailVerificationSendFailureException(
                details = mapOf("snuMail" to request.snuMail),
            )
        }
    }

    fun checkSnuMailVerification(request: CheckSnuMailVerificationRequest) {
        val encryptedCode =
            userRedisCacheService.getEmailCode(request.snuMail)
                ?: throw EmailVerificationInvalidException(
                    details = mapOf("snuMail" to request.snuMail),
                )

        // 입력된 인증 코드와 Redis에 저장된 암호화된 코드 비교
        if (!BCrypt.checkpw(request.code, encryptedCode)) {
            throw EmailVerificationInvalidException(
                details = mapOf("snuMail" to request.snuMail),
            )
        } else {
            userRedisCacheService.deleteEmailCode(request.snuMail)
        }
    }

    @Value("\${custom.SECRET}")
    private lateinit var resetDbSecret: String

    fun resetDatabase(secret: String) {
        if (secret != resetDbSecret) {
            throw InvalidRequestException(
                details = mapOf("providedSecret" to secret),
            )
        }
        userRepository.deleteAll()
        userRedisCacheService.deleteAll()
    }
}
