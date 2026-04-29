plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}
dependencies {
    //  모든 모듈 (:domain, :data, :presentation) 공통 이동
    testImplementation(kotlin("test"))

    //  일반적인 단위 테스트용 (도메인, 데이터, 프레젠테이션 모두 사용 가능) -   필요한 경우 사용
    testImplementation(libs.junit)
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    // Hilt가 아니라, @Inject 어노테이션만 쓸 수 있게 해주는 가벼운 라이브러리입니다.
    implementation("javax.inject:javax.inject:1")
}