package com.waffletoy.team1server.auth

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class CustomOAuth2UserService : DefaultOAuth2UserService() {
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)

        // 클라이언트 정보 가져오기
        val clientRegistrationId = userRequest.clientRegistration.registrationId
        val userNameAttributeName = userRequest.clientRegistration.providerDetails.userInfoEndpoint.userNameAttributeName

        // 사용자 정보 맵핑
        val attributes = oAuth2User.attributes
        val oAuth2Attribute = OAuth2Attribute.of(clientRegistrationId, userNameAttributeName, attributes)

        return DefaultOAuth2User(
            setOf(SimpleGrantedAuthority("ROLE_USER")), // 권한 목록
            oAuth2Attribute.mapAttribute(),
            "email",
        )
    }
}
