package com.waffletoy.team1server

import io.github.cdimascio.dotenv.Dotenv
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.servers.Server
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import java.util.*

@OpenAPIDefinition(
    servers = [
        Server(
            url = "https://api.survey-josha.site",
            description = "Default Server URL",
        ),
        Server(
            url = "https://www.api.survey-josha.site",
            description = "Alias Server URL",
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
    // Load .env file
    val dotenv =
        Dotenv.configure()
            .directory("/app") // .env 파일이 루트 디렉토리에 있을 경우
            .ignoreIfMissing() // .env 파일이 없으면 무시
            .load()

    // Set environment variables
    dotenv.entries().forEach { entry ->
        System.setProperty(entry.key, entry.value)
    }
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))
    runApplication<Application>(*args)
}
