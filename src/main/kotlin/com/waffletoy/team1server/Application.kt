package com.waffletoy.team1server

import io.github.cdimascio.dotenv.Dotenv
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.servers.Server
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@OpenAPIDefinition(
    servers = [
        Server(
            url = "https://www.survey-josha.site",
            description = "Default Server URL",
        ),
        Server(
            url = "http://localhost:8080",
            description = "Local Test URL",
        ),
    ],
)
@SpringBootApplication
@EnableJpaAuditing
class Application

fun main(args: Array<String>) {
    // .env 변수 가져오기
    val dotenv = Dotenv.load()
    System.setProperty("GOOGLE_CLIENT_ID", dotenv["GOOGLE_CLIENT_ID"] ?: throw IllegalArgumentException("GOOGLE_CLIENT_ID not set"))
    System.setProperty("GOOGLE_CLIENT_SECRET", dotenv["GOOGLE_CLIENT_SECRET"] ?: throw IllegalArgumentException("GOOGLE_CLIENT_SECRET not set"))
    System.setProperty("GOOGLE_AUTH_URI", dotenv["GOOGLE_AUTH_URI"] ?: throw IllegalArgumentException("GOOGLE_AUTH_URI not set"))
    System.setProperty("GOOGLE_TOKEN_URI", dotenv["GOOGLE_TOKEN_URI"] ?: throw IllegalArgumentException("GOOGLE_TOKEN_URI not set"))

    System.setProperty("TOKEN_PRIVATE_KEY", dotenv["TOKEN_PRIVATE_KEY"] ?: throw IllegalArgumentException("TOKEN_PRIVATE_KEY not set"))

    runApplication<Application>(*args)
}
