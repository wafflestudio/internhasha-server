package com.wafflestudio.internhasha.company.web.controller

import com.wafflestudio.internhasha.auth.domain.RoleValue
import com.wafflestudio.internhasha.auth.domain.User
import com.wafflestudio.internhasha.auth.web.AuthUser
import com.wafflestudio.internhasha.company.application.service.CompanyService
import com.wafflestudio.internhasha.company.web.dto.CompanyResponse
import com.wafflestudio.internhasha.company.web.dto.CreateCompanyRequest
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/company")
@PreAuthorize("hasAuthority('${RoleValue.ROLE_COMPANY}')")
class CompanyController(
    private val companyService: CompanyService,
) {
    @PutMapping("/me")
    fun putCompany(
        @Parameter(hidden = true) @AuthUser user: User,
        @Valid @RequestBody request: CreateCompanyRequest,
    ): ResponseEntity<CompanyResponse> {
        val company = companyService.putCompany(request.toCommand(user.id))
        return ResponseEntity.ok(CompanyResponse.fromCompany(company))
    }

    @GetMapping("/me")
    fun getCompany(
        @Parameter(hidden = true) @AuthUser user: User,
    ): ResponseEntity<CompanyResponse> {
        val company = companyService.getCompany(user.id)
        return ResponseEntity.ok(CompanyResponse.fromCompany(company))
    }
}
