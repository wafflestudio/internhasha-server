package com.waffletoy.team1server.company.controller

import com.waffletoy.team1server.auth.AuthUser
import com.waffletoy.team1server.auth.dto.User
import com.waffletoy.team1server.company.dto.Company
import com.waffletoy.team1server.company.dto.CreateCompanyRequest
import com.waffletoy.team1server.company.service.CompanyService
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/company")
class CompanyController(
    private val companyService: CompanyService,
) {
    @PutMapping("/me")
    fun putCompany(
        @Parameter(hidden = true) @AuthUser user: User,
        @Valid @RequestBody request: CreateCompanyRequest,
    ): ResponseEntity<Company> {
        val company = companyService.putCompany(user, request)
        return ResponseEntity.ok(company)
    }

//    @DeleteMapping("/{company_id}")
//    fun deleteCompany(
//        @Parameter(hidden = true) @AuthUser user: User,
//        @PathVariable("company_id") companyId: String,
//    ): ResponseEntity<Void> {
//        companyService.deleteCompany(user, companyId)
//        return ResponseEntity.ok().build()
//    }

    @GetMapping("/me")
    fun getCompanyByCompany(
        @Parameter(hidden = true) @AuthUser user: User,
    ): ResponseEntity<List<Company>> {
        val companies = companyService.getCompanyByCompany(user)
        return ResponseEntity.ok(companies)
    }
}
