package com.waffletoy.team1server.user.service

import com.waffletoy.team1server.exceptions.OAuthFailedException
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

@Service
class GoogleOAuth2Client(
    private val restTemplate: RestTemplate,
) {
    fun getUserInfo(accessToken: String): GoogleUserInfo {
        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer $accessToken")
        val request = HttpEntity<Void>(headers)

        return try {
            val response =
                restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v3/userinfo",
                    HttpMethod.GET,
                    request,
                    GoogleUserInfo::class.java,
                )
            response.body ?: throw OAuthFailedException() // 빈 body
        } catch (ex: HttpClientErrorException) {
            throw OAuthFailedException()
        } catch (ex: Exception) {
            throw OAuthFailedException() // TODO Internal error가 맞나?
        }
    }
}

data class GoogleUserInfo(
    val sub: String,
    val email: String,
    val name: String,
)
