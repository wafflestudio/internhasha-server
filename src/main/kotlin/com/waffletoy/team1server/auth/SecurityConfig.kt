package com.waffletoy.team1server.auth

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig(
    private val oAuth2SuccessHandler: OAuth2SuccessHandler,
    private val customOAuth2UserService: CustomOAuth2UserService,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { csrf -> csrf.disable() } // CSRF 비활성화
            .authorizeHttpRequests { requests ->
                requests
                    .requestMatchers("/api/social-google").authenticated() // 공개 엔드포인트
                    .anyRequest().permitAll() // 나머지 요청은 인증 필요
            }
            .oauth2Login { oauth2 ->
                oauth2.authorizationEndpoint { auth ->
                    auth.baseUri("/oauth2/authorization") // 인증 요청 엔드포인트
                }
                    .redirectionEndpoint { redir ->
                        redir.baseUri("/login/oauth2/code/**") // 리다이렉션 엔드포인트
                    }
                    .userInfoEndpoint { userInfo ->
                        userInfo.userService(customOAuth2UserService) // 사용자 정보 처리
                    }
                    .successHandler(oAuth2SuccessHandler) // 성공 핸들러
            }

        return http.build()
    }
}
