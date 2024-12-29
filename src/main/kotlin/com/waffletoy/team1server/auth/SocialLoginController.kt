package com.waffletoy.team1server.auth

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView

@RestController
@RequestMapping("/api")
class SocialLoginController {
    @GetMapping("/social-google")
    fun socialGoogle(): RedirectView {
        return RedirectView("/oauth2/authorization/google")
    }
}
