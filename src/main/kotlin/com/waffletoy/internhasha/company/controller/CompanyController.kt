package com.waffletoy.internhasha.company.controller

import com.waffletoy.internhasha.auth.AuthUser
import com.waffletoy.internhasha.auth.dto.User
import com.waffletoy.internhasha.company.dto.Company
import com.waffletoy.internhasha.company.dto.CreateCompanyRequest
import com.waffletoy.internhasha.company.service.CompanyService
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
    fun getCompany(
        @Parameter(hidden = true) @AuthUser user: User,
    ): ResponseEntity<Company> {
        val companies = companyService.getCompany(user)
        return ResponseEntity.ok(companies)
    }
}
