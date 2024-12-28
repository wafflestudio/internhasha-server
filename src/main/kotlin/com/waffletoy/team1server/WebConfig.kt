package com.waffletoy.team1server

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**") // 모든 경로에 대해 CORS 허용
            .allowedOrigins(
                "https://survey-josha.site",
                "https://www.survey-josha.site",
                "https://d3lb937auepw3n.cloudfront.net",
                "https://d1vq7k80ej9gk7.cloudfront.net",
                "http://localhost:5173",
            ) // 허용할 도메인
            .allowedMethods("GET", "POST", "PUT", "DELETE") // 허용할 HTTP 메서드
            .allowedHeaders("*") // 허용할 헤더
            .allowCredentials(true) // 인증 정보(쿠키 등) 허용
    }
}
