package com.waffletoy.team1server.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests { authz ->
                authz
                    .requestMatchers("/logout").authenticated() // 로그아웃은 인증된 사용자만 접근 가능
                    .anyRequest().permitAll() // 그 외 모든 요청은 자유롭게 접근 가능
            }
            .logout { it.disable() } // 커스텀 로그아웃 API 사용 시 Spring Security 기본 로그아웃 비활성화

        return http.build()
    }
}
