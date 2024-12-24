package com.waffletoy.team1server.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/test")
class TestController {
    @GetMapping
    fun getTestMessage(): String {
        return "The server is running successfully!"
    }
}
