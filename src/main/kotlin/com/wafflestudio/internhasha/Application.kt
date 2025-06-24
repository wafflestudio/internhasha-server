package com.wafflestudio.internhasha

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import java.util.*

@SpringBootApplication
@EnableJpaAuditing
class Application

fun main(args: Array<String>) {
    // 서울 타임존 설정
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))
    runApplication<Application>(*args)
}
