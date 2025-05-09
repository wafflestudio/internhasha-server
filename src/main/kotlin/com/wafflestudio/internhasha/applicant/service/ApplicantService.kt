package com.wafflestudio.internhasha.applicant.service

import com.wafflestudio.internhasha.applicant.ApplicantNotFoundException
import com.wafflestudio.internhasha.applicant.ApplicantUserForbiddenException
import com.wafflestudio.internhasha.applicant.dto.ApplicantResponse
import com.wafflestudio.internhasha.applicant.dto.PutApplicantRequest
import com.wafflestudio.internhasha.applicant.persistence.ApplicantEntity
import com.wafflestudio.internhasha.applicant.persistence.ApplicantRepository
import com.wafflestudio.internhasha.auth.UserNotFoundException
import com.wafflestudio.internhasha.auth.UserRole
import com.wafflestudio.internhasha.auth.dto.User
import com.wafflestudio.internhasha.auth.persistence.UserEntity
import com.wafflestudio.internhasha.auth.persistence.UserRepository
import com.wafflestudio.internhasha.s3.service.S3Service
import org.springframework.context.annotation.Lazy
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ApplicantService(
    val applicantRepository: ApplicantRepository,
    val s3Service: S3Service,
    @Lazy private val userRepository: UserRepository,
) {
    fun getApplicant(
        user: User,
    ): ApplicantEntity {
        // User가 Applicant가 맞는지 확인
        if (user.userRole != UserRole.APPLICANT) {
            throw ApplicantUserForbiddenException(
                details = mapOf("userId" to user.id, "userRole" to user.userRole),
            )
        }

        // User의 Applicant 정보 받기
        val applicantEntity: ApplicantEntity? = applicantRepository.findByUserId(user.id)

        // applicant null이면 정보 없음 반환
        if (applicantEntity == null) {
            throw ApplicantNotFoundException()
        }

        // 정보 반환
        return applicantEntity
    }

    fun putApplicant(
        user: User,
        request: PutApplicantRequest,
    ): ApplicantResponse {
        // user가 Applicant 맞는지 확인
        if (user.userRole != UserRole.APPLICANT) {
            throw ApplicantUserForbiddenException(
                details = mapOf("userId" to user.id, "userRole" to user.userRole),
            )
        }

        val userEntity: UserEntity? = userRepository.findByIdOrNull(user.id)
        if (userEntity == null) {
            throw UserNotFoundException()
        }

        val applicantEntity: ApplicantEntity? = applicantRepository.findByUserId(user.id)

        // 기존 s3 object 삭제

        applicantEntity?.let { applicant ->
            applicant.cvKey.let { if (it != request.cvKey) s3Service.deleteS3File(it) }
            applicant.profileImageKey?.let { if (it != request.imageKey) s3Service.deleteS3File(it) }
            applicant.portfolioKey?.let { if (it != request.portfolioKey) s3Service.deleteS3File(it) }
        }

        var updatedApplicant =
            applicantEntity?.apply {
                updatedAt = LocalDateTime.now()
                enrollYear = request.enrollYear
                dept = request.department
                positions = request.positions
                slogan = request.slogan
                explanation = request.explanation
                stacks = request.stacks
                profileImageKey = request.imageKey
                cvKey = request.cvKey
                portfolioKey = request.portfolioKey
                links = request.links
            } ?: ApplicantEntity(
                user = userEntity,
                enrollYear = request.enrollYear,
                dept = request.department,
                positions = request.positions,
                slogan = request.slogan,
                explanation = request.explanation,
                stacks = request.stacks,
                profileImageKey = request.imageKey,
                cvKey = request.cvKey,
                portfolioKey = request.portfolioKey,
                links = request.links,
            )

        updatedApplicant = applicantRepository.saveAndFlush(updatedApplicant)

        return ApplicantResponse.fromEntity(updatedApplicant)
    }
}
