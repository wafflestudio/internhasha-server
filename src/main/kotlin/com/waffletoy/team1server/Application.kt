package com.waffletoy.team1server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import java.util.*

@SpringBootApplication
@EnableJpaAuditing
class Application

fun main(args: Array<String>) {
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))
//    runApplication<Application>(*args)
    SpringApplicationBuilder(Application::class.java)
        .applicationStartup(BufferingApplicationStartup(2048))
        .run(*args)
}
