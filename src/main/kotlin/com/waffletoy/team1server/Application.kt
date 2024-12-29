package com.waffletoy.team1server

import io.github.cdimascio.dotenv.Dotenv
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.servers.Server
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@OpenAPIDefinition(
    servers = [
        Server(
            url = "https://www.survey-josha.site",
            description = "Default Server URL",
        ),
    ],
)
@SpringBootApplication
class Application

fun main(args: Array<String>) {
    // .env 변수 가져오기
    val dotenv = Dotenv.load()
    System.setProperty("GOOGLE_CLIENT_ID", dotenv["GOOGLE_CLIENT_ID"] ?: throw IllegalArgumentException("GOOGLE_CLIENT_ID not set"))
    System.setProperty("GOOGLE_CLIENT_SECRET", dotenv["GOOGLE_CLIENT_SECRET"] ?: throw IllegalArgumentException("GOOGLE_CLIENT_SECRET not set"))
    System.setProperty("GOOGLE_AUTH_URI", dotenv["GOOGLE_AUTH_URI"] ?: throw IllegalArgumentException("GOOGLE_AUTH_URI not set"))
    System.setProperty("GOOGLE_TOKEN_URI", dotenv["GOOGLE_TOKEN_URI"] ?: throw IllegalArgumentException("GOOGLE_TOKEN_URI not set"))

    runApplication<Application>(*args)
}
