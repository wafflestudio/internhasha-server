package com.waffletoy.team1server.applicant.controller

import com.waffletoy.team1server.auth.AuthUser
import com.waffletoy.team1server.auth.dto.User
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/applicant")
class ApplicantController(

) {
    @GetMapping("/me")
    fun getMe(
        @Parameter(hidden = true) @AuthUser user: User,
    ) : ResponseEntity<User> {
        TODO("ApplicantService.getMeByUser")
        return ResponseEntity.ok(user)
    }
}