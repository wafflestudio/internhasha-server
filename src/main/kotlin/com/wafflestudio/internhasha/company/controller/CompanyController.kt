package com.wafflestudio.internhasha.company.controller

import com.wafflestudio.internhasha.auth.domain.RoleValue
import com.wafflestudio.internhasha.auth.security.AuthUser
import com.wafflestudio.internhasha.auth.security.AuthUserPrincipal
import com.wafflestudio.internhasha.company.controller.dto.CompanyResponse
import com.wafflestudio.internhasha.company.controller.dto.CreateCompanyRequest
import com.wafflestudio.internhasha.company.service.CompanyService
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
        @Parameter(hidden = true) @AuthUser user: AuthUserPrincipal,
        @Valid @RequestBody request: CreateCompanyRequest,
    ): ResponseEntity<CompanyResponse> {
        val result = companyService.putCompany(request.toCommand(user.id))
        return ResponseEntity.ok(CompanyResponse.fromDto(result))
    }

    @GetMapping("/me")
    fun getCompany(
        @Parameter(hidden = true) @AuthUser user: AuthUserPrincipal,
    ): ResponseEntity<CompanyResponse> {
        val result = companyService.getCompany(user.id)
        return ResponseEntity.ok(CompanyResponse.fromDto(result))
    }
}
