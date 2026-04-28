package com.wafflestudio.internhasha.auth.service

import com.wafflestudio.internhasha.applicant.domain.Applicant
import com.wafflestudio.internhasha.auth.exception.UserDuplicateLocalIdException
import com.wafflestudio.internhasha.auth.exception.UserSuccessCodeException
import com.wafflestudio.internhasha.auth.repository.AuthRedisRepository
import com.wafflestudio.internhasha.auth.repository.UserRepository
import com.wafflestudio.internhasha.auth.security.TokenProvider
import com.wafflestudio.internhasha.auth.service.dto.SignUpCommand
import com.wafflestudio.internhasha.auth.service.dto.UserSimple
import com.wafflestudio.internhasha.auth.service.dto.UserWithTokens
import com.wafflestudio.internhasha.company.domain.Company
import com.wafflestudio.internhasha.exceptions.InvalidCredentialsException
import org.mindrot.jbcrypt.BCrypt
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class SignUpService(
    private val userRepository: UserRepository,
    private val authRedisRepository: AuthRedisRepository,
    private val tokenProvider: TokenProvider,
    @Value("\${custom.DEV_SECRET}")
    private val devSecret: String,
) {
    @Transactional
    fun signUp(command: SignUpCommand): UserWithTokens {
        val userSimple: UserSimple =
            when (command.info) {
                is SignUpCommand.LocalApplicantInfo -> {
                    localApplicantSignUp(command.info)
                }

                is SignUpCommand.LocalCompanyInfo -> {
                    localCompanySignUp(command.info)
                }
            }
        val tokens = tokenProvider.generateTokens(userSimple)
        authRedisRepository.saveRefreshToken(userSimple.id, tokens.refreshToken)
        return UserWithTokens(userSimple, tokens)
    }

    private fun localApplicantSignUp(info: SignUpCommand.LocalApplicantInfo): UserSimple {
        // 이메일(아이디) 중복 확인
        if (userRepository.existsByEmailAndDeletedAtIsNull(info.email)) {
            throw UserDuplicateLocalIdException(
                details = mapOf("email" to info.email),
            )
        }

        // 성공 코드 확인(실제로 메일 인증으로 발급한 성공 코드 or 비밀코드)
        if (info.successCode != devSecret) {
            if (!authRedisRepository.verifySuccessCode(info.email, info.successCode)) {
                throw UserSuccessCodeException(details = mapOf("email" to info.email))
            }
            authRedisRepository.deleteSuccessCode(info.email)
        }

        val applicant =
            userRepository.save(
                Applicant(
                    name = info.name,
                    email = info.email,
                    passwordHash = BCrypt.hashpw(info.password, BCrypt.gensalt()),
                ),
            )

        return UserSimple(applicant.requireId(), applicant.getRole())
    }

    private fun localCompanySignUp(info: SignUpCommand.LocalCompanyInfo): UserSimple {
        // 관리자 비밀번호 확인
        if (info.secretPassword != devSecret) {
            throw InvalidCredentialsException(
                details = mapOf("secretPassword" to "invalidSecretPassword"),
            )
        }

        // 이메일(아이디) 중복 확인
        if (userRepository.existsByEmailAndDeletedAtIsNull(info.email)) {
            throw UserDuplicateLocalIdException(
                details = mapOf("email" to info.email),
            )
        }

        val company =
            userRepository.save(
                Company(
                    name = info.name,
                    email = info.email,
                    passwordHash = BCrypt.hashpw(info.password, BCrypt.gensalt()),
                    vcName = info.vcName,
                    vcRec = info.vcRecommendation,
                ),
            )

        return UserSimple(company.requireId(), company.getRole())
    }
}
