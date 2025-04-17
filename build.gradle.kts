
plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.9.25"
}
group = "com.waffletoy"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("com.h2database:h2:2.2.220")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.hibernate.validator:hibernate-validator:8.0.0.Final")
    testImplementation("org.glassfish:jakarta.el:5.0.0-M1")

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")

    implementation("com.mysql:mysql-connector-j:8.2.0")

    // google auth
//    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
//    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    // redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // jsonwebtoken
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // email
    implementation("org.springframework.boot:spring-boot-starter-mail")

    // s3
    implementation("com.amazonaws:aws-java-sdk-s3:1.12.540")

    // jsoup
    implementation("org.jsoup:jsoup:1.18.3")

    implementation("com.amazonaws:aws-java-sdk:1.12.779")

    // thymeleaf
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    // h2
    runtimeOnly("com.h2database:h2")

    // Spring boot actuator
    implementation("org.springframework.boot:spring-boot-starter-actuator")
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

tasks.withType<Test> {
    useJUnitPlatform()
}
