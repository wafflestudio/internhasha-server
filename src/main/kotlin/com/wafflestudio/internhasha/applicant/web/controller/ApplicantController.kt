package com.wafflestudio.internhasha.applicant.web.controller

import com.wafflestudio.internhasha.applicant.application.service.ApplicantService
import com.wafflestudio.internhasha.applicant.web.dto.ApplicantResponse
import com.wafflestudio.internhasha.applicant.web.dto.PutApplicantRequest
import com.wafflestudio.internhasha.auth.domain.RoleValue
import com.wafflestudio.internhasha.auth.domain.User
import com.wafflestudio.internhasha.auth.web.AuthUser
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/applicant")
@PreAuthorize("hasAuthority('${RoleValue.ROLE_APPLICANT}')")
class ApplicantController(
    private val applicantService: ApplicantService,
) {
    @GetMapping("/me")
    fun getApplicant(
        @Parameter(hidden = true) @AuthUser user: User,
    ): ResponseEntity<ApplicantResponse> {
        val response = ApplicantResponse.fromApplicant(applicantService.getApplicant(user))
        return ResponseEntity.ok(response)
    }

    @PutMapping("/me")
    fun putApplicant(
        @Parameter(hidden = true) @AuthUser user: User,
        @Valid @RequestBody request: PutApplicantRequest,
    ): ResponseEntity<ApplicantResponse> {
        val command = request.toCommand(user.id)
        val response = ApplicantResponse.fromApplicant(applicantService.putApplicant(command))
        return ResponseEntity.ok(response)
    }
}
