package com.waffletoy.team1server.resume.controller

import com.waffletoy.team1server.account.AuthUser
import com.waffletoy.team1server.account.AuthenticateException
import com.waffletoy.team1server.account.controller.User
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/api/resume")
@Validated
class ResumeController (
    private val resumeService: ResumeService,
) {
    // 커피챗 상세 페이지 불러오기
    @GetMapping("/{resumeId")
    fun getResumeDetail(
        @AuthUser user: User?,
        @PathVariable resumeId: String,
    ) : ResponseEntity<Resume> {
        if (user == null) throw AuthenticateException("유효하지 않은 엑세스 토큰입니다.")
        return resumeService.getResumeDetail(resumeId)
    }

    // 커피챗 상세 페이지 불러오기
    @GetMapping
    fun getResumeDetail(
        @AuthUser user: User?,
        @PathVariable resumeId: String,
    ) : ResponseEntity<Resume> {
        if (user == null) throw AuthenticateException("유효하지 않은 엑세스 토큰입니다.")
        return resumeService.getResumeDetail(resumeId)
    }

}