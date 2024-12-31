package com.waffletoy.team1server.user.service

import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class GoogleOAuth2Client(
    private val restTemplate: RestTemplate,
) {
    fun getUserInfo(accessToken: String): GoogleUserInfo {
        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer $accessToken")
        val request = HttpEntity<Void>(headers)

        val response =
            restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v3/userinfo",
                HttpMethod.GET,
                request,
                GoogleUserInfo::class.java,
            )

        return response.body ?: throw IllegalArgumentException("Failed to fetch user info from Google")
    }
}

data class GoogleUserInfo(
    val email: String,
    val name: String,
)
