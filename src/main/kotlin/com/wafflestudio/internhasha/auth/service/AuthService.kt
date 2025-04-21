package com.wafflestudio.internhasha.auth.service

import com.wafflestudio.internhasha.auth.*
import com.wafflestudio.internhasha.auth.controller.*
import com.wafflestudio.internhasha.auth.dto.*
import com.wafflestudio.internhasha.auth.persistence.UserEntity
import com.wafflestudio.internhasha.auth.persistence.UserRepository
import com.wafflestudio.internhasha.auth.utils.PasswordGenerator
import com.wafflestudio.internhasha.auth.utils.UserTokenUtil
import com.wafflestudio.internhasha.coffeeChat.service.CoffeeChatService
import com.wafflestudio.internhasha.company.persistence.CompanyEntity
import com.wafflestudio.internhasha.email.EmailSendFailureException
import com.wafflestudio.internhasha.email.EmailType
import com.wafflestudio.internhasha.email.service.EmailService
import com.wafflestudio.internhasha.exceptions.*
import com.wafflestudio.internhasha.post.service.PostService
import com.wafflestudio.internhasha.s3.service.S3Service
import org.mindrot.jbcrypt.BCrypt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Lazy
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val authRedisCacheService: AuthRedisCacheService,
    private val s3Service: S3Service,
    @Lazy private val emailService: EmailService,
//    @Lazy private val coffeeChatService: CoffeeChatService,
//    @Lazy private val postService: PostService,
) {
    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Transactional
    fun signUp(request: SignUpRequest): Pair<User, UserTokenUtil.Tokens> {
        val user: User =
            when (request.authType) {
                UserRole.APPLICANT -> {
                    val info = request.info as SignUpRequest.LocalApplicantInfo
                    localApplicantSignUp(info)
                }

                UserRole.COMPANY -> {
                    val info = request.info as SignUpRequest.LocalCompanyInfo
                    localCompanySignUp(info)
                }
            }
        val tokens = UserTokenUtil.generateTokens(user)

        // 발급 받은 refresh token을 redis에 저장합니다.
        authRedisCacheService.saveRefreshToken(user.id, tokens.refreshToken)
        return Pair(user, tokens)
    }

    private fun localApplicantSignUp(info: SignUpRequest.LocalApplicantInfo): User {
        // 이미 존재하는 계정 확인
        val existingUser = userRepository.findByEmail(info.email)
        existingUser?.let {
            if (it.userRole != UserRole.APPLICANT) {
                throw NotAuthorizedException(
                    details = mapOf("userId" to it.id, "userRole" to it.userRole),
                )
            }
            throw UserDuplicateSnuMailException(
                details = mapOf("snuMail" to info.email),
            )
        }

        // 이메일(아이디) 중복 확인
        if (userRepository.existsByEmail(info.email)) {
            throw UserDuplicateLocalIdException(
                details = mapOf("email" to info.email),
            )
        }

        // 성공 코드 확인(실제로 메일 인증으로 발급한 성공 코드 or 비밀코드)
        if (info.successCode != devSecret) {
            if (authRedisCacheService.getSuccessCode(info.successCode)) {
                authRedisCacheService.deleteSuccessCode(info.successCode)
            } else {
                throw UserSuccessCodeException(details = mapOf("user" to info.name, "successCode" to info.successCode))
            }
        }

        // 새로운 사용자 생성
        val user =
            UserEntity(
                name = info.name,
                email = info.email,
                passwordHash = BCrypt.hashpw(info.password, BCrypt.gensalt()),
                userRole = UserRole.APPLICANT,
            ).let { userRepository.save(it) }

        return User.fromEntity(user)
    }

    private fun localCompanySignUp(info: SignUpRequest.LocalCompanyInfo): User {
        // 관리자 비밀번호 확인
        if (info.secretPassword != devSecret) {
            throw InvalidCredentialsException(
                details = mapOf("secretPassword" to info.secretPassword),
            )
        }

        // 이메일(아이디) 중복 확인
        if (userRepository.existsByEmail(info.email)) {
            throw UserDuplicateLocalIdException(
                details = mapOf("email" to info.email),
            )
        }

        // 새로운 사용자 생성
        val user =
            UserEntity(
                name = info.name,
                email = info.email,
                passwordHash = BCrypt.hashpw(info.password, BCrypt.gensalt()),
                userRole = UserRole.COMPANY,
            ).let { userRepository.save(it) }

        if (info.vcName != null || info.vcRecommendation != null) {
            val newCompany = CompanyEntity(user = user)

            if (info.vcName != null) newCompany.vcName = info.vcName
            if (info.vcRecommendation != null) newCompany.vcRec = info.vcRecommendation

            user.company = newCompany
            userRepository.save(user)
        }

        return User.fromEntity(user)
    }

    // 로그인
    @Transactional
    fun signIn(request: SignInRequest): Pair<User, UserTokenUtil.Tokens> {
        val userEntity =
            userRepository.findByEmail(request.email)
                ?: throw InvalidCredentialsException()

        if (!BCrypt.checkpw(request.password, userEntity.passwordHash)) {
            throw InvalidCredentialsException()
        }

        val user = User.fromEntity(userEntity)

        // 기존 refresh token 을 만료합니다.(RTR)
        authRedisCacheService.deleteRefreshTokenByUserId(user.id)

        val tokens = UserTokenUtil.generateTokens(user)

        // 발급 받은 refresh token을 redis에 저장합니다.
        authRedisCacheService.saveRefreshToken(user.id, tokens.refreshToken)

        return Pair(user, tokens)
    }

    fun signOut(
        user: User,
        refreshToken: String,
    ) {
        val userId =
            authRedisCacheService.getUserIdByRefreshToken(refreshToken)
                ?: throw InvalidRefreshTokenException(
                    details = mapOf("refreshToken" to refreshToken),
                )
        if (user.id != userId) {
            throw TokenMismatchException(
                details = mapOf("userId" to user.id, "refreshTokenUserId" to userId),
            )
        }

        // 로그아웃 시 Refresh Token 삭제
        // (Access Token 은 클라이언트 측에서 삭제)
        authRedisCacheService.deleteRefreshTokenByUserId(user.id)

        // 추후 유저의 Access Token 을 Access Token 의 남은 유효시간 만큼
        // Redis 블랙리스트에 추가할 필요성 있음
    }

    @Transactional
    fun refreshAccessToken(refreshToken: String): UserTokenUtil.Tokens {
        val userId =
            authRedisCacheService.getUserIdByRefreshToken(refreshToken)
                ?: throw InvalidRefreshTokenException(
                    details = mapOf("refreshToken" to refreshToken),
                )

        // 기존 refresh token 을 만료합니다.(RTR)
        authRedisCacheService.deleteRefreshTokenByUserId(userId)

        val userEntity =
            userRepository.findByIdOrNull(userId)
                ?: throw UserNotFoundException(
                    details = mapOf("userId" to userId),
                )

        val tokens = UserTokenUtil.generateTokens(User.fromEntity(entity = userEntity))
        // 발급 받은 refresh token을 redis에 저장합니다.
        authRedisCacheService.saveRefreshToken(userEntity.id, tokens.refreshToken)

        return tokens
    }

    // 메일(아이디) 중복 확인
    @Transactional(readOnly = true)
    fun checkDuplicateMail(request: EmailRequest) {
        if (userRepository.existsByEmail(request.email)) {
            throw UserDuplicateSnuMailException(
                details = mapOf("email" to request.email),
            )
        }
    }

    // Email verification
    fun sendSnuMailVerification(request: SnuMailRequest) {
        val emailCode = (100000..999999).random().toString()
        val encryptedEmailCode = BCrypt.hashpw(emailCode, BCrypt.gensalt())

        authRedisCacheService.saveEmailCode(request.snuMail, encryptedEmailCode)
        try {
            emailService.sendEmail(
                type = EmailType.VerifyMail,
                to = request.snuMail,
                subject = "[인턴하샤] 이메일 인증 안내",
                text = emailCode,
            )
        } catch (ex: Exception) {
            throw EmailSendFailureException(
                details = mapOf("snuMail" to request.snuMail),
            )
        }
    }

    fun checkSnuMailVerification(
        request: CheckSnuMailVerificationRequest,
    ): String {
        val encryptedCode =
            authRedisCacheService.getEmailCode(request.snuMail)
                ?: throw UserEmailVerificationInvalidException(
                    details = mapOf("snuMail" to request.snuMail),
                )

        // 입력된 인증 코드와 Redis에 저장된 암호화된 코드 비교
        if (!BCrypt.checkpw(request.code, encryptedCode)) {
            throw UserEmailVerificationInvalidException(
                details = mapOf("snuMail" to request.snuMail),
            )
        } else {
            authRedisCacheService.deleteEmailCode(request.snuMail)
            val successCode = UUID.randomUUID().toString()
            authRedisCacheService.saveSuccessCode(successCode)
            return successCode
        }
    }

    @Transactional
    fun withdrawUser(user: User) {
        if (user.userRole != UserRole.APPLICANT) {
            throw NotAuthorizedException(
                details = mapOf("userId" to user.id, "userRole" to user.userRole),
            )
        }

        val userEntity =
            userRepository.findByIdOrNull(user.id)
                ?: throw UserNotFoundException(
                    details = mapOf("userId" to user.id),
                )

        // 순환 의존성 해결: 동적으로 빈을 가져와서 사용
        val postService = applicationContext.getBean(PostService::class.java)
        val coffeeChatService = applicationContext.getBean(CoffeeChatService::class.java)

        // 일방향 외래키 제약이 걸려있는 bookmark, coffeeChat 삭제
        postService.deleteBookmarkByUser(userEntity)
        coffeeChatService.deleteCoffeeChatByUser(userEntity)
        userRepository.deleteUserEntityById(user.id)
        authRedisCacheService.deleteRefreshTokenByUserId(user.id)

        // s3 object 삭제
        userEntity.applicant?.let { applicant ->
            applicant.cvKey?.let { s3Service.deleteS3File(it) }
            applicant.profileImageKey?.let { s3Service.deleteS3File(it) }
            applicant.portfolioKey?.let { s3Service.deleteS3File(it) }
        }

        // Applicant Entity 는 cascade 삭제
    }

    @Transactional
    fun changePassword(
        user: User,
        passwordRequest: ChangePasswordRequest,
    ) {
        val userEntity =
            userRepository.findByIdOrNull(user.id)
                ?: throw UserNotFoundException(
                    details = mapOf("userId" to user.id),
                )

        // 기존 비밀번호를 비교
        if (!BCrypt.checkpw(passwordRequest.oldPassword, userEntity.passwordHash)) {
            throw InvalidCredentialsException(
                details = mapOf("oldPassword" to passwordRequest.oldPassword),
            )
        }

        // 새 비밀번호를 저장
        userEntity.passwordHash = BCrypt.hashpw(passwordRequest.newPassword, BCrypt.gensalt())
        userRepository.save(userEntity)
    }

    @Transactional
    fun resetPassword(emailRequest: EmailRequest) {
        // 메일을 기준으로 유저 찾기
        val user =
            userRepository.findByEmail(emailRequest.email)
                ?: throw UserNotFoundException(
                    details = mapOf("email" to emailRequest.email),
                )

        val newPassword = PasswordGenerator.generateRandomPassword()

        // 새 비밀번호를 저장
        user.passwordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt())
        userRepository.save(user)

        // 로컬 계정 유저의 정보를 제공 or 소셜 로그인 정보를 제공
        try {
            emailService.sendEmail(
                type = EmailType.ResetPassword,
                to = user.email,
                subject = "[인턴하샤] 임시 비밀번호 발급 안내",
                text = newPassword,
            )
        } catch (ex: Exception) {
            throw EmailSendFailureException(
                details = mapOf("email" to user.email),
            )
        }
    }

    // 다른 서비스에서 UserId로 User 가져오기
    @Transactional(readOnly = true)
    fun getUserEntityByUserId(userId: String): UserEntity? = userRepository.findByIdOrNull(userId)

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

    fun makeDummyUser(index: Int): UserEntity {
        return userRepository.findByEmail("dummy$index@gmail.com")
            ?: userRepository.save(
                UserEntity(
                    name = "dummy$index",
                    email = "dummy$index@gmail.com",
                    passwordHash = BCrypt.hashpw("DummyPW$index!99", BCrypt.gensalt()),
                    userRole = UserRole.COMPANY,
                ),
            )
    }

    @Value("\${custom.SECRET}")
    private lateinit var devSecret: String
}
