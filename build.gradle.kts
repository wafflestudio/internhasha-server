
plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    kotlin("plugin.jpa") version "2.2.21"

    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1"

    id("org.jetbrains.kotlin.plugin.allopen") version "2.2.21"
    id("com.google.devtools.ksp") version "2.2.21-2.0.4"
}
group = "com.internhasha"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    // spring-waffle code artifact
    maven {
        val authToken =
            properties["codeArtifactAuthToken"] as String? ?: ProcessBuilder(
                "aws",
                "codeartifact",
                "get-authorization-token",
                "--domain",
                "wafflestudio",
                "--domain-owner",
                "405906814034",
                "--query",
                "authorizationToken",
                "--region",
                "ap-northeast-1",
                "--output",
                "text",
            ).start().inputStream.bufferedReader().readText().trim()
        url = uri("https://wafflestudio-405906814034.d.codeartifact.ap-northeast-1.amazonaws.com/maven/spring-waffle/")
        credentials {
            username = "aws"
            password = authToken
        }
    }
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.hibernate.validator:hibernate-validator:8.0.0.Final")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")

    // MySQL
    implementation("com.mysql:mysql-connector-j:8.2.0")

    // AWS SDK
//    implementation("com.amazonaws:aws-java-sdk:1.12.782")
    implementation("com.amazonaws:aws-java-sdk-s3:1.12.782")
    implementation("com.amazonaws:aws-java-sdk-cloudfront:1.12.782")

    // JWT
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    implementation("io.jsonwebtoken:jjwt-impl:0.13.0")
    implementation("io.jsonwebtoken:jjwt-jackson:0.13.0")

    // HTML Parser
    implementation("org.jsoup:jsoup:1.18.3")

    // QueryDSL
    implementation("io.github.openfeign.querydsl:querydsl-jpa:7.0")
    ksp("io.github.openfeign.querydsl:querydsl-ksp-codegen:7.0")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.mockk:mockk:1.13.14")
    testImplementation("com.ninja-squad:springmockk:5.0.1")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")

    // H2
    implementation("com.h2database:h2:2.2.220")

    // spring-waffle for secrets
    implementation("com.wafflestudio.spring:spring-boot-starter-waffle:1.0.2")

    // Devtools
    developmentOnly("org.springframework.boot:spring-boot-devtools")
//    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
//    testImplementation("org.glassfish:jakarta.el:5.0.0-M1")
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

ktlint {
    filter {
        exclude { element ->
            element.file.invariantSeparatorsPath.contains("/build/generated/")
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    archiveFileName.set("app.jar")
}
