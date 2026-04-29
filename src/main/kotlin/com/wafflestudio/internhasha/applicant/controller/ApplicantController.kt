package com.wafflestudio.internhasha.applicant.controller

import com.wafflestudio.internhasha.applicant.controller.dto.ApplicantResponse
import com.wafflestudio.internhasha.applicant.controller.dto.PutApplicantRequest
import com.wafflestudio.internhasha.applicant.service.ApplicantService
import com.wafflestudio.internhasha.auth.domain.RoleValue
import com.wafflestudio.internhasha.auth.security.AuthUser
import com.wafflestudio.internhasha.auth.security.AuthUserPrincipal
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
        @Parameter(hidden = true) @AuthUser authUser: AuthUserPrincipal,
    ): ResponseEntity<ApplicantResponse> {
        val result = applicantService.getApplicant(authUser)
        return ResponseEntity.ok(ApplicantResponse.fromDto(result))
    }

    @PutMapping("/me")
    fun putApplicant(
        @Parameter(hidden = true) @AuthUser authUser: AuthUserPrincipal,
        @Valid @RequestBody request: PutApplicantRequest,
    ): ResponseEntity<ApplicantResponse> {
        val command = request.toCommand(authUser.id)
        val result = applicantService.putApplicant(command)
        return ResponseEntity.ok(ApplicantResponse.fromDto(result))
    }
}
