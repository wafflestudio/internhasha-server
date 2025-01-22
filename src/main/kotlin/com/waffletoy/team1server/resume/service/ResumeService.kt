// File: com/waffletoy/team1server/post/service/ResumeService.kt
package com.waffletoy.team1server.resume.service

import com.waffletoy.team1server.email.service.EmailService
import com.waffletoy.team1server.exceptions.*
import com.waffletoy.team1server.post.*
import com.waffletoy.team1server.post.persistence.RoleRepository
import com.waffletoy.team1server.resume.*
import com.waffletoy.team1server.resume.controller.*
import com.waffletoy.team1server.resume.controller.Resume
import com.waffletoy.team1server.resume.persistence.ResumeEntity
import com.waffletoy.team1server.resume.persistence.ResumeRepository
import com.waffletoy.team1server.user.UserRole
import com.waffletoy.team1server.user.dtos.User
import com.waffletoy.team1server.user.persistence.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ResumeService(
    private val resumeRepository: ResumeRepository,
    private val userRepository: UserRepository,
    private val emailService: EmailService,
    private val roleRepository: RoleRepository,
) {
    @Value("\${custom.page.size:12}")
    private val pageSize: Int = 12

    /**
     * Retrieves detailed information of a specific resume by its ID.
     *
     * @param user The authenticated user.
     * @param resumeId The unique identifier of the resume.
     * @return The detailed [Resume] object.
     * @throws ResumeNotFoundException If the resume with the given ID does not exist.
     * @throws ResumeForbiddenException If the user is not the owner of the resume.
     * @throws InvalidAccessTokenException If the user is not authenticated.
     */
    fun getResumeDetail(
        user: User?,
        resumeId: String,
    ): Resume {
        val validUser = getValidUser(user)
        val resumeEntity =
            resumeRepository.findByIdOrNull(resumeId)
                ?: throw ResumeNotFoundException(
                    details = mapOf("resumeId" to resumeId),
                )
        if (resumeEntity.user.id != validUser.id) {
            throw ResumeForbiddenException(
                details = mapOf("userId" to validUser.id, "resumeId" to resumeId),
            )
        }
        return Resume.fromEntity(resumeEntity)
    }

    /**
     * Retrieves a list of resumes belonging to the authenticated user.
     *
     * @param user The authenticated user.
     * @return A list of [Resume] objects.
     * @throws ResumeNotFoundException If the user does not exist.
     * @throws InvalidAccessTokenException If the user is not authenticated.
     */
    fun getResumes(
        user: User?,
    ): List<Resume> {
        val validUser = getValidUser(user)
        return userRepository.findByIdOrNull(validUser.id)?.let { userEntity ->
            userEntity.resumes.map { Resume.fromEntity(it) }
        } ?: throw ResumeNotFoundException(
            details = mapOf("userId" to validUser.id),
        )
    }

    /**
     * Creates a new resume associated with a specific post.
     *
     * @param user The authenticated user.
     * @param postId The unique identifier of the post.
     * @param coffee The [Coffee] data containing resume details.
     * @return The created [Resume] object.
     * @throws ResumeNotFoundException If the post does not exist.
     * @throws ResumeCreationFailedException If there is an issue creating the resume.
     * @throws ResumeNotFoundException If the user does not exist.
     * @throws ResumeForbiddenException If the user is not authenticated or has an invalid role.
     */
    @Transactional
    fun postResume(
        user: User?,
        postId: String,
        coffee: Coffee,
    ): Resume {
        val validUser = getValidUser(user)
        val userEntity =
            userRepository.findByIdOrNull(validUser.id)
                ?: throw ResumeNotFoundException(
                    details = mapOf("userId" to validUser.id),
                )

        val roleEntity =
            roleRepository.findByIdOrNull(postId)
                ?: throw ResumeNotFoundException(
                    details = mapOf("postId" to postId),
                )

        val resumeEntity =
            try {
                resumeRepository.save(
                    ResumeEntity(
                        content = coffee.content,
                        phoneNumber = coffee.phoneNumber,
                        role = roleEntity,
                        user = userEntity,
                    ),
                )
            } catch (ex: Exception) {
                throw ResumeCreationFailedException(
                    details =
                        mapOf(
                            "userId" to validUser.id,
                            "postId" to postId,
                            "error" to ex.message.orEmpty(),
                        ),
                )
            }

        val companyEntity = roleEntity.company

        // 이메일 전송
        try {
            emailService.sendEmail(
                to = companyEntity.email,
                subject = "[인턴하샤] 지원자 커피챗이 도착하였습니다.",
                text =
                    """
                    [${companyEntity.companyName}] ${roleEntity.title} 포지션 지원자 정보:
                    
                    - 회사명: ${companyEntity.companyName}
                    - 회사 이메일: ${companyEntity.email}
                    - 직무명: ${roleEntity.title}
                    - 카테고리: ${roleEntity.category}
                    - 지원 마감일: ${roleEntity.employmentEndDate ?: "정보 없음"}
                    
                    지원자 정보:
                    - 이름: ${validUser.name}
                    - 이메일: ${validUser.snuMail ?: "이메일 정보 없음"}
                    - 전화번호: ${resumeEntity.phoneNumber ?: "전화번호 정보 없음"}
                    
                    커피챗 내용:
                    --------------------------------------------
                    ${resumeEntity.content ?: "커피챗 내용이 없습니다."}
                    --------------------------------------------
                    
                    인턴하샤 지원 시스템을 통해 지원자가 회사에 커피챗을 제출하였습니다.
                    """.trimIndent(),
            )
        } catch (ex: Exception) {
            throw EmailSendFailureException(
                details =
                    mapOf(
                        "to" to companyEntity.email,
                        "error" to ex.message.orEmpty(),
                    ),
            )
        }

        return Resume.fromEntity(resumeEntity)
    }

    /**
     * Deletes a specific resume.
     *
     * @param user The authenticated user.
     * @param resumeId The unique identifier of the resume.
     * @throws ResumeNotFoundException If the resume does not exist.
     * @throws ResumeForbiddenException If the user is not the owner of the resume.
     * @throws ResumeDeletionFailedException If there is an issue deleting the resume.
     * @throws InvalidAccessTokenException If the user is not authenticated.
     */
    @Transactional
    fun deleteResume(
        user: User?,
        resumeId: String,
    ) {
        val validUser = getValidUser(user)
        val resumeEntity =
            resumeRepository.findByIdOrNull(resumeId)
                ?: throw ResumeNotFoundException(
                    details = mapOf("resumeId" to resumeId),
                )
        if (resumeEntity.user.id != validUser.id) {
            throw ResumeForbiddenException(
                details = mapOf("userId" to validUser.id, "resumeId" to resumeId),
            )
        }
        try {
            resumeRepository.delete(resumeEntity)
        } catch (ex: Exception) {
            throw ResumeDeletionFailedException(
                details =
                    mapOf(
                        "resumeId" to resumeId,
                        "error" to ex.message.orEmpty(),
                    ),
            )
        }
    }

    /**
     * Updates an existing resume.
     *
     * @param user The authenticated user.
     * @param resumeId The unique identifier of the resume.
     * @param coffee The [Coffee] data containing updated resume details.
     * @return The updated [Resume] object.
     * @throws ResumeNotFoundException If the resume does not exist.
     * @throws ResumeForbiddenException If the user is not the owner of the resume.
     * @throws ResumeUpdateFailedException If there is an issue updating the resume.
     * @throws InvalidAccessTokenException If the user is not authenticated.
     */
    @Transactional
    fun patchResume(
        user: User?,
        resumeId: String,
        coffee: Coffee,
    ): Resume {
        val validUser = getValidUser(user)
        val resumeEntity =
            resumeRepository.findByIdOrNull(resumeId)
                ?: throw ResumeNotFoundException(
                    details = mapOf("resumeId" to resumeId),
                )

        // 작성자가 맞는지 확인
        if (resumeEntity.user.id != validUser.id) {
            throw ResumeForbiddenException(
                details = mapOf("userId" to validUser.id, "resumeId" to resumeId),
            )
        }

        // 전달된 데이터로 업데이트
        resumeEntity.phoneNumber = coffee.phoneNumber
        resumeEntity.content = coffee.content

        return try {
            Resume.fromEntity(resumeRepository.save(resumeEntity))
        } catch (ex: Exception) {
            throw ResumeUpdateFailedException(
                details =
                    mapOf(
                        "resumeId" to resumeId,
                        "error" to ex.message.orEmpty(),
                    ),
            )
        }
    }

    /**
     * Validates the authenticated user.
     *
     * @param user The authenticated user.
     * @return The validated [User] object.
     * @throws InvalidAccessTokenException If the user is not authenticated.
     * @throws ResumeForbiddenException If the user does not have the NORMAL role.
     */
    fun getValidUser(user: User?): User {
        if (user == null) {
            throw InvalidAccessTokenException(
                details = mapOf("user" to "null"),
            )
        }
        if (user.userRole != UserRole.NORMAL) {
            throw ResumeForbiddenException(
                details = mapOf("userId" to user.id, "userRole" to user.userRole),
            )
        }
        return user
    }
}
