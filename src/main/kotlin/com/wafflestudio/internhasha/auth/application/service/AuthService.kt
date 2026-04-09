package com.wafflestudio.internhasha.auth.application.service

import com.wafflestudio.internhasha.auth.application.dto.ChangePasswordCommand
import com.wafflestudio.internhasha.auth.application.dto.CheckDuplicateMailCommand
import com.wafflestudio.internhasha.auth.application.dto.CheckSnuMailVerificationCommand
import com.wafflestudio.internhasha.auth.application.dto.ResetPasswordCommand
import com.wafflestudio.internhasha.auth.application.dto.SendSnuMailVerificationCommand
import com.wafflestudio.internhasha.auth.application.dto.SignInCommand
import com.wafflestudio.internhasha.auth.application.dto.SignUpCommand
import com.wafflestudio.internhasha.auth.application.port.RefreshTokenPort
import com.wafflestudio.internhasha.auth.application.port.TokenProviderPort
import com.wafflestudio.internhasha.auth.application.port.UserPersistencePort
import com.wafflestudio.internhasha.auth.application.port.VerificationCodePort
import com.wafflestudio.internhasha.auth.domain.Tokens
import com.wafflestudio.internhasha.auth.domain.User
import com.wafflestudio.internhasha.auth.domain.UserApplicant
import com.wafflestudio.internhasha.auth.domain.UserCompany
import com.wafflestudio.internhasha.auth.domain.UserDuplicateLocalIdException
import com.wafflestudio.internhasha.auth.domain.UserDuplicateSnuMailException
import com.wafflestudio.internhasha.auth.domain.UserEmailVerificationInvalidException
import com.wafflestudio.internhasha.auth.domain.UserNotFoundException
import com.wafflestudio.internhasha.auth.domain.UserRole
import com.wafflestudio.internhasha.auth.domain.UserSimple
import com.wafflestudio.internhasha.auth.domain.UserSuccessCodeException
import com.wafflestudio.internhasha.auth.security.PasswordGenerator
import com.wafflestudio.internhasha.common.event.EmailSendEvent
import com.wafflestudio.internhasha.common.event.UserDeletedEvent
import com.wafflestudio.internhasha.email.EmailType
import com.wafflestudio.internhasha.exceptions.*
import org.mindrot.jbcrypt.BCrypt
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class AuthService(
    private val userPersistencePort: UserPersistencePort,
    private val refreshTokenPort: RefreshTokenPort,
    private val verificationCodePort: VerificationCodePort,
    private val tokenProviderPort: TokenProviderPort,
    private val applicationEventPublisher: ApplicationEventPublisher,
    @Value("\${custom.DEV_SECRET}")
    private val devSecret: String,
) {
    @Transactional
    fun signUp(command: SignUpCommand): Pair<User, Tokens> {
        val user: User =
            when (command.info) {
                is SignUpCommand.LocalApplicantInfo -> {
                    localApplicantSignUp(command.info)
                }

                is SignUpCommand.LocalCompanyInfo -> {
                    localCompanySignUp(command.info)
                }
            }
        val tokens = tokenProviderPort.generateTokens(UserSimple(user.id, user.userRole))

        refreshTokenPort.saveRefreshToken(user.id, tokens.refreshToken)
        return Pair(user, tokens)
    }

    private fun localApplicantSignUp(info: SignUpCommand.LocalApplicantInfo): User {
        // 이메일(아이디) 중복 확인
        if (userPersistencePort.existsUserByEmail(info.email)) {
            throw UserDuplicateLocalIdException(
                details = mapOf("email" to info.email),
            )
        }

        // 성공 코드 확인(실제로 메일 인증으로 발급한 성공 코드 or 비밀코드)
        if (info.successCode != devSecret) {
            if (!verificationCodePort.verifySuccessCode(info.email, info.successCode)) {
                throw UserSuccessCodeException(details = mapOf("user" to info.name, "successCode" to info.successCode))
            }
            verificationCodePort.deleteSuccessCode(info.email)
        }

        val user =
            userPersistencePort.saveUser(
                UserApplicant(
                    id = "",
                    name = info.name,
                    email = info.email,
                    passwordHash = BCrypt.hashpw(info.password, BCrypt.gensalt()),
                    userRole = UserRole.ROLE_APPLICANT,
                ),
            )

        return user
    }

    private fun localCompanySignUp(info: SignUpCommand.LocalCompanyInfo): User {
        // 관리자 비밀번호 확인
        if (info.secretPassword != devSecret) {
            throw InvalidCredentialsException(
                details = mapOf("secretPassword" to info.secretPassword),
            )
        }

        // 이메일(아이디) 중복 확인
        if (userPersistencePort.existsUserByEmail(info.email)) {
            throw UserDuplicateLocalIdException(
                details = mapOf("email" to info.email),
            )
        }

        val user =
            userPersistencePort.saveUser(
                UserCompany(
                    id = "",
                    name = info.name,
                    email = info.email,
                    passwordHash = BCrypt.hashpw(info.password, BCrypt.gensalt()),
                    userRole = UserRole.ROLE_COMPANY,
                    vcName = info.vcName,
                    vcRec = info.vcRecommendation,
                ),
            )

        return user
    }

    // 로그인
    fun signIn(command: SignInCommand): Pair<UserSimple, Tokens> {
        val user =
            userPersistencePort.findUserByEmail(command.email)
                ?: throw InvalidCredentialsException()
        if (!BCrypt.checkpw(command.password, user.passwordHash)) {
            throw InvalidCredentialsException()
        }

        // refresh token을 redis에 저장
        // 기존 refresh token 삭제는 생략 - 다중 디바이스 로그인 지원
        val userSimple = UserSimple.fromUser(user)
        val tokens = tokenProviderPort.generateTokens(userSimple)
        refreshTokenPort.saveRefreshToken(userSimple.id, tokens.refreshToken)

        return Pair(userSimple, tokens)
    }

    fun signOut(
        userId: String,
    ) {
        // 로그아웃 시 Refresh Token 삭제 (Access Token 은 클라이언트 측에서 삭제)
        val result = refreshTokenPort.deleteRefreshToken(userId)
        if (!result) {
            throw InvalidRefreshTokenException(
                details = mapOf("userId" to userId, "refreshToken" to "Invalid or already expired"),
            )
        }

        // 추후 유저의 Access Token 을 Access Token 의 남은 유효시간 만큼
        // Redis 블랙리스트에 추가할 필요성 있음
    }

    fun refreshAccessToken(refreshToken: String): Tokens {
        val userId =
            tokenProviderPort.getUserIdFromToken(refreshToken)
                ?: throw InvalidRefreshTokenException(
                    details = mapOf("refreshToken" to refreshToken),
                )
        val savedRefreshToken =
            refreshTokenPort.getRefreshToken(userId) ?: throw InvalidRefreshTokenException(
                details = mapOf("userId" to userId, "refreshToken" to "Invalid or already expired"),
            )

        if (refreshToken != savedRefreshToken) {
            refreshTokenPort.deleteRefreshToken(userId)
            throw InvalidRefreshTokenException(
                details = mapOf("userId" to userId, "refreshToken" to "Reused"),
            )
        }

        val userSimple =
            userPersistencePort.findUserById(userId)?.let { UserSimple.fromUser(it) }
                ?: throw UserNotFoundException(
                    details = mapOf("userId" to userId),
                )

        val tokens = tokenProviderPort.generateTokens(userSimple)
        refreshTokenPort.saveRefreshToken(requireNotNull(userSimple.id), tokens.refreshToken)
        return tokens
    }

    // 메일(아이디) 중복 확인
    fun checkDuplicateMail(command: CheckDuplicateMailCommand) {
        if (userPersistencePort.existsUserByEmail(command.email)) {
            throw UserDuplicateSnuMailException(
                details = mapOf("email" to command.email),
            )
        }
    }

    // Email verification
    fun sendSnuMailVerification(command: SendSnuMailVerificationCommand) {
        if (userPersistencePort.existsUserByEmail(command.snuMail)) {
            throw UserDuplicateSnuMailException(
                details = mapOf("email" to command.snuMail),
            )
        }

        val emailCode = (100000..999999).random().toString()
        val encryptedEmailCode = BCrypt.hashpw(emailCode, BCrypt.gensalt())

        verificationCodePort.saveEmailCode(command.snuMail, encryptedEmailCode)
        applicationEventPublisher.publishEvent(
            EmailSendEvent(
                emailType = EmailType.VerifyMail,
                to = command.snuMail,
                subject = "[인턴하샤] 이메일 인증 안내",
                text = emailCode,
            ),
        )
    }

    fun checkSnuMailVerification(
        command: CheckSnuMailVerificationCommand,
    ): String {
        val result = verificationCodePort.verifyEmailCode(command.snuMail, command.code)
        if (!result) {
            throw UserEmailVerificationInvalidException(
                details = mapOf("snuMail" to command.snuMail),
            )
        } else {
            verificationCodePort.deleteEmailCode(command.snuMail)
            val successCode = UUID.randomUUID().toString()
            verificationCodePort.saveSuccessCode(command.snuMail, successCode)
            return successCode
        }
    }

    @Transactional
    fun withdrawUser(user: User) {
        // SoftDelete 적용
        userPersistencePort.deleteUser(user.id)
        applicationEventPublisher.publishEvent(UserDeletedEvent(user.id, user.userRole))

//        // 일방향 외래키 제약이 걸려있는 bookmark, coffeeChat 삭제
//        postPersistenceAdapter.deleteAllBookmarkByUserId(user.id)
//        coffeeChatPersistenceAdapter.deleteAllByUserId(user.id)
//        refreshTokenPort.deleteRefreshToken(user.id)
//        verificationCodePort.deleteEmailCode(user.id)
//        verificationCodePort.deleteSuccessCode(user.id)
    }

    @Transactional
    fun changePassword(command: ChangePasswordCommand) {
        val userEntity =
            userPersistencePort.findUserById(command.userId)
                ?: throw UserNotFoundException(
                    details = mapOf("userId" to command.userId),
                )

        if (!BCrypt.checkpw(command.oldPassword, userEntity.passwordHash)) {
            throw InvalidCredentialsException(
                details = mapOf("oldPassword" to command.oldPassword),
            )
        }

        // 새 비밀번호로 갱신
        val newPasswordHash = BCrypt.hashpw(command.newPassword, BCrypt.gensalt())
        userPersistencePort.changePassword(command.userId, newPasswordHash)
    }

    @Transactional
    fun resetPassword(command: ResetPasswordCommand) {
        // 메일을 기준으로 유저 찾기
        val user =
            userPersistencePort.findUserByEmail(command.email)
                ?: throw UserNotFoundException(
                    details = mapOf("email" to command.email),
                )

        val newPassword = PasswordGenerator.generateRandomPassword()
        val newPasswordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt())
        userPersistencePort.changePassword(user.id, newPasswordHash)

        // 이메일 발송은 비동기로 진행
        applicationEventPublisher.publishEvent(
            EmailSendEvent(
                emailType = EmailType.ResetPassword,
                to = user.email,
                subject = "[인턴하샤] 임시 비밀번호 발급 안내",
                text = newPassword,
            ),
        )
    }

    fun authenticate(accessToken: String): User {
        val userId =
            tokenProviderPort.getUserIdFromToken(accessToken)
                ?: throw InvalidAccessTokenException()

        return userPersistencePort.findUserById(userId)
            ?: throw UserNotFoundException(
                details = mapOf("userId" to userId),
            )
    }
}
