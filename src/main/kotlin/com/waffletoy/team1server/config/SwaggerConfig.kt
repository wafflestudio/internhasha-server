package com.waffletoy.team1server.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
@Profile("local", "dev", "prod")
class SwaggerConfig(
    @Value("\${custom.protocol}") private val protocol: String,
    @Value("\${custom.domain-name}") private val domain: String,
) {
    @Bean
    @Primary
    fun customOpenAPI(): OpenAPI {
        val url = "$protocol://$domain"

        return OpenAPI()
            .components(
                Components().addSecuritySchemes(
                    "BearerAuth",
                    SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"),
                ),
            )
            .addSecurityItem(
                SecurityRequirement().addList("BearerAuth"),
            )
            .servers(
                listOf(
                    Server().url(url).description("Server URL"),
                ),
            )
    }
}
