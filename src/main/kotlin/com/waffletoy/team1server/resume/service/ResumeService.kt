package com.waffletoy.team1server.resume.service

import com.waffletoy.team1server.account.persistence.UserRepository
import com.waffletoy.team1server.post.persistence.CompanyRepository
import com.waffletoy.team1server.post.persistence.RoleRepository
import com.waffletoy.team1server.resume.ResumeServiceException
import com.waffletoy.team1server.resume.controller.Resume
import com.waffletoy.team1server.resume.persistence.ResumeEntity
import com.waffletoy.team1server.resume.persistence.ResumeRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ResumeService(
    private val resumeRepository: ResumeRepository,
    private val userRepository: UserRepository,
    private val companyRepository: CompanyRepository,
    private val roleRepository: RoleRepository,
) {
    fun getResumeDetail(
        userId: String,
        resumeId: String,
    ): Resume {
        val resumeEntity =
            resumeRepository.findByIdOrNull(resumeId)
                ?: throw ResumeServiceException(
                    "해당 페이지가 존재하지 않습니다.",
                    HttpStatus.NOT_FOUND,
                )
        if (resumeEntity.user.id != userId) {
            throw ResumeServiceException(
                "커피챗 작성자가 아닙니다",
                HttpStatus.FORBIDDEN,
            )
        }
        return Resume.fromEntity(resumeEntity)
    }

    fun getResumes(
        userId: String,
    ): List<Resume> {
        val userEntity =
            userRepository.findByIdOrNull(userId)
                ?: throw ResumeServiceException(
                    "존재하지 않는 사용자입니다.",
                    HttpStatus.NOT_FOUND,
                )
        return userEntity.resumes.map { Resume.fromEntity(it) }
    }

    @Transactional
    fun postResume(
        userId: String,
        postId: String,
        phoneNumber: String,
        content: String,
    ): Resume {
        val userEntity =
            userRepository.findByIdOrNull(userId)
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
                    content = content,
                    phoneNumber = phoneNumber,
                    role = roleEntity,
                    user = userEntity,
                ),
            )
        return Resume.fromEntity(resumeEntity)
    }

    fun deleteResume(
        userId: String,
        resumeId: String,
    ) {
        val resumeEntity =
            resumeRepository.findByIdOrNull(resumeId)
                ?: throw ResumeServiceException(
                    "커피챗이 존재하지 않습니다.",
                )
        if (resumeEntity.user.id != userId) {
            throw ResumeServiceException(
                "커피챗 작성자가 아닙니다",
                HttpStatus.FORBIDDEN,
                1,
            )
        }
        resumeRepository.delete(resumeEntity)
    }

    fun patchResume(
        userId: String,
        resumeId: String,
        phoneNumber: String,
        content: String,
    ): Resume {
        val resumeEntity =
            resumeRepository.findByIdOrNull(resumeId)
                ?: throw ResumeServiceException(
                    "존재하지 않는 커피챗입니다.",
                    HttpStatus.NOT_FOUND,
                )

        // 작성자가 맞는지 확인
        if (resumeEntity.user.id != userId) {
            throw ResumeServiceException(
                "커피챗 작성자가 아닙니다",
                HttpStatus.FORBIDDEN,
                1,
            )
        }

        // 전달된 데이터로 업데이트
        phoneNumber.let {
            // phoneNumber를 저장하는 필드가 없다면 무시하거나 다른 로직으로 처리
            resumeEntity.phoneNumber = it
        }

        content.let {
            resumeEntity.content = it
        }

        // 데이터베이스에 저장(수정 시간은 자동 갱신)
        return Resume.fromEntity(
            resumeRepository.save(resumeEntity),
        )
    }
}
