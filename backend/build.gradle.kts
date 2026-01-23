import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    kotlin("plugin.jpa") version "1.9.22"
    jacoco
}

group = "com.fanpulse"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Resilience4j
    implementation("io.github.resilience4j:resilience4j-spring-boot3:2.2.0")
    implementation("io.github.resilience4j:resilience4j-kotlin:2.2.0")

    // Micrometer
    implementation("io.micrometer:micrometer-registry-prometheus")

    // ShedLock (W4: 스케줄러 동시 실행 방지)
    implementation("net.javacrumbs.shedlock:shedlock-spring:5.10.2")
    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:5.10.2")

    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.3")

    // Database
    runtimeOnly("org.postgresql:postgresql")

    // Flyway Migration (Spring Boot managed version)
    implementation("org.flywaydb:flyway-core")

    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    // SpringDoc OpenAPI (Swagger)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    // Spring Security
    implementation("org.springframework.boot:spring-boot-starter-security")
    testImplementation("org.springframework.security:spring-security-test")

    // JWT (jjwt)
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")

    // Google ID Token Verification
    implementation("com.google.api-client:google-api-client:2.2.0")

    // Rate Limiting (Bucket4j)
    implementation("com.bucket4j:bucket4j-core:8.7.0")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.mockk:mockk:1.13.9")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    testImplementation("org.awaitility:awaitility-kotlin:4.2.0")
    testImplementation("com.github.tomakehurst:wiremock-jre8-standalone:3.0.1")
    testImplementation("org.testcontainers:testcontainers:1.19.4")
    testImplementation("org.testcontainers:postgresql:1.19.4")
    testImplementation("org.testcontainers:junit-jupiter:1.19.4")

    // Kotlin Coroutines Test
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // H2 for tests
    testRuntimeOnly("com.h2database:h2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = true
        html.required = true
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        // Application 서비스: 80% (핵심 비즈니스 로직)
        rule {
            element = "PACKAGE"
            includes = listOf("com.fanpulse.application.*")
            limit {
                counter = "INSTRUCTION"
                minimum = "0.80".toBigDecimal()
            }
        }
        // Domain: 50% (점진적으로 높여갈 예정)
        // common 패키지는 인터페이스/추상 클래스라 제외
        rule {
            element = "PACKAGE"
            includes = listOf("com.fanpulse.domain.*")
            excludes = listOf("com.fanpulse.domain.common")
            limit {
                counter = "INSTRUCTION"
                minimum = "0.50".toBigDecimal()
            }
        }
        // Infrastructure (외부 연동): 40%
        rule {
            element = "PACKAGE"
            includes = listOf("com.fanpulse.infrastructure.*")
            limit {
                counter = "INSTRUCTION"
                minimum = "0.40".toBigDecimal()
            }
        }
    }
}
