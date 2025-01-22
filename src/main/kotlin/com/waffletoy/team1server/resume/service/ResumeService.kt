package com.waffletoy.team1server.resume.service

import com.waffletoy.team1server.email.service.EmailService
import com.waffletoy.team1server.post.persistence.RoleRepository
import com.waffletoy.team1server.resume.ResumeServiceException
import com.waffletoy.team1server.resume.controller.Coffee
import com.waffletoy.team1server.resume.controller.Resume
import com.waffletoy.team1server.resume.persistence.ResumeEntity
import com.waffletoy.team1server.resume.persistence.ResumeRepository
import com.waffletoy.team1server.user.AuthenticateException
import com.waffletoy.team1server.user.Role
import com.waffletoy.team1server.user.dtos.User
import com.waffletoy.team1server.user.persistence.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ResumeService(
    private val resumeRepository: ResumeRepository,
    private val userRepository: UserRepository,
    private val emailService: EmailService,
    private val roleRepository: RoleRepository,
) {
    fun getResumeDetail(
        user: User?,
        resumeId: String,
    ): Resume {
        val validUser = getValidUser(user)
        val resumeEntity =
            resumeRepository.findByIdOrNull(resumeId)
                ?: throw ResumeServiceException(
                    "해당 페이지가 존재하지 않습니다.",
                    HttpStatus.NOT_FOUND,
                )
        if (resumeEntity.user.id != validUser.id) {
            throw ResumeServiceException(
                "커피챗 작성자가 아닙니다",
                HttpStatus.FORBIDDEN,
            )
        }
        return Resume.fromEntity(resumeEntity)
    }

    fun getResumes(
        user: User?,
    ): List<Resume> {
        val validUser = getValidUser(user)
        return userRepository.findByIdOrNull(validUser.id)?.let { userEntity ->
            userEntity.resumes.map { Resume.fromEntity(it) }
        } ?: throw ResumeServiceException(
            "존재하지 않는 사용자입니다.",
            HttpStatus.NOT_FOUND,
        )
    }

    @Transactional
    fun postResume(
        user: User?,
        postId: String,
        coffee: Coffee,
    ): Resume {
        val validUser = getValidUser(user)
        val userEntity =
            userRepository.findByIdOrNull(validUser.id)
                ?: throw ResumeServiceException(
                    "존재하지 않는 사용자입니다.",
                    HttpStatus.NOT_FOUND,
                )

        val roleEntity =
            roleRepository.findByIdOrNull(postId)
                ?: throw ResumeServiceException(
                    "존재하지 않는 게시글입니다.",
                    HttpStatus.NOT_FOUND,
                    1,
                )

        val resumeEntity =
            resumeRepository.save(
                ResumeEntity(
                    content = coffee.content,
                    phoneNumber = coffee.phoneNumber,
                    role = roleEntity,
                    user = userEntity,
                ),
            )

        val companyEntity = roleEntity.company

        // 이메일 전송
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

        return Resume.fromEntity(resumeEntity)
    }

    fun deleteResume(
        user: User?,
        resumeId: String,
    ) {
        val validUser = getValidUser(user)
        val resumeEntity =
            resumeRepository.findByIdOrNull(resumeId)
                ?: throw ResumeServiceException(
                    "커피챗이 존재하지 않습니다.",
                )
        if (resumeEntity.user.id != validUser.id) {
            throw ResumeServiceException(
                "커피챗 작성자가 아닙니다",
                HttpStatus.FORBIDDEN,
                1,
            )
        }
        resumeRepository.delete(resumeEntity)
    }

    fun patchResume(
        user: User?,
        resumeId: String,
        coffee: Coffee,
    ): Resume {
        val validUser = getValidUser(user)
        val resumeEntity =
            resumeRepository.findByIdOrNull(resumeId)
                ?: throw ResumeServiceException(
                    "존재하지 않는 커피챗입니다.",
                    HttpStatus.NOT_FOUND,
                )

        // 작성자가 맞는지 확인
        if (resumeEntity.user.id != validUser.id) {
            throw ResumeServiceException(
                "커피챗 작성자가 아닙니다",
                HttpStatus.FORBIDDEN,
                1,
            )
        }

        // 전달된 데이터로 업데이트
        coffee.phoneNumber.let {
            // phoneNumber를 저장하는 필드가 없다면 무시하거나 다른 로직으로 처리
            resumeEntity.phoneNumber = it
        }

        coffee.content.let {
            resumeEntity.content = it
        }

        // 데이터베이스에 저장(수정 시간은 자동 갱신)
        return Resume.fromEntity(
            resumeRepository.save(resumeEntity),
        )
    }

    fun getValidUser(user: User?): User {
        if (user == null) throw AuthenticateException("유효하지 않은 엑세스 토큰입니다.")
        if (user.role != Role.ROLE_APPLICANT) {
            throw ResumeServiceException(
                "유효한 사용자 역할이 아닙니다.",
                HttpStatus.FORBIDDEN,
                1,
            )
        }
        return user
    }
}
