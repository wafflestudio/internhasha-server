package com.waffletoy.team1server.user.service

import com.waffletoy.team1server.user.GoogleOAuthServiceException
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
            response.body ?: throw GoogleOAuthServiceException(
                "구글에서 받아온 사용자 정보가 비어있습니다(Empty body)",
                HttpStatus.BAD_REQUEST)
        } catch (ex: HttpClientErrorException) {
            throw GoogleOAuthServiceException(
                "구글에서 사용자 정보를 받아오는데 실패했습니다.(HttpClientError) - ${ex.statusCode} - ${ex.responseBodyAsString}",
                HttpStatus.BAD_REQUEST)
        } catch (ex: Exception) {
            throw GoogleOAuthServiceException(
                "구글에서 사용자 정보를 받아오는데 실패했습니다. $ex"
            )
        }
    }
}

data class GoogleUserInfo(
    val sub: String,
    val email: String,
    val name: String,
)
