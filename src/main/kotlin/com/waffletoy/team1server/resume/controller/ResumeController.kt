package com.waffletoy.team1server.resume.controller

import com.waffletoy.team1server.resume.service.ResumeService
import com.waffletoy.team1server.user.AuthUser
import com.waffletoy.team1server.user.dtos.User
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/resume")
@Validated
class ResumeController(
    private val resumeService: ResumeService,
) {
    // 커피챗 상세 페이지 불러오기
    @GetMapping("/{resumeId}")
    fun getResumeDetail(
        @AuthUser user: User?,
        @PathVariable resumeId: String,
    ): ResponseEntity<Resume> {
        return ResponseEntity.ok(
            resumeService.getResumeDetail(user, resumeId),
        )
    }

    // 커피챗 목록 불러오기
    @GetMapping
    fun getResumes(
        @AuthUser user: User?,
    ): ResponseEntity<Resumes> {
        return ResponseEntity.ok(
            Resumes(
                resumeList = resumeService.getResumes(user),
            ),
        )
    }

    // 커피챗 신청하기
    @PostMapping("/{postId}")
    fun postResume(
        @AuthUser user: User?,
        @PathVariable postId: String,
        @RequestBody coffee: Coffee,
    ): ResponseEntity<Resume> {
        val resume =
            resumeService.postResume(
                user,
                postId,
                coffee,
            )
        return ResponseEntity.ok(resume)
    }

    // 커피챗 삭제하기
    @DeleteMapping("/{resumeId}")
    fun deleteResume(
        @AuthUser user: User?,
        @PathVariable resumeId: String,
    ): ResponseEntity<Void> {
        resumeService.deleteResume(user, resumeId)
        return ResponseEntity.ok().build()
    }

    // 커피챗 수정하기
    @PatchMapping("/{resumeId}")
    fun patchResume(
        @AuthUser user: User?,
        @PathVariable resumeId: String,
        @RequestBody coffee: Coffee,
    ): ResponseEntity<Resume> {
        val updatedResume = resumeService.patchResume(user, resumeId, coffee)
        return ResponseEntity.ok(updatedResume)
    }
}

data class Resumes(
    val resumeList: List<Resume>,
)

data class Coffee(
    val phoneNumber: String,
    val content: String,
)
