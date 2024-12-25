package com.waffletoy.team1server.pretotype.controller

import com.waffletoy.team1server.pretotype.service.PretotypeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class TestResponseController(
    private val pretotypeService: PretotypeService,
) {
    @PostMapping("/api/pretotype")
    fun createPretotype(
        @RequestBody request: PretotypeRequest,
    ): ResponseEntity<Pretotype> {
        val pretotype = pretotypeService.createPretotype(request.email, request.isSubscribed)
        return ResponseEntity.ok(pretotype)
    }

    @GetMapping("/api/pretotype/list")
    fun listPretotypes(): ResponseEntity<List<Pretotype>> {
        val pretotypes = pretotypeService.listPretotypes()
        return ResponseEntity.ok(pretotypes)
    }
}

data class PretotypeRequest(
    val email: String,
    val isSubscribed: Boolean,
)
