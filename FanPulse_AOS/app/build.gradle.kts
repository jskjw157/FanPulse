import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.aos.fanpulse"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.aos.fanpulse"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin.compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":presentation"))

    //  모든 모듈 (:domain, :data, :presentation) 공통 이동
    testImplementation(kotlin("test"))

    //  hilt -  공통 사항
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    //  일반적인 단위 테스트용 (도메인, 데이터, 프레젠테이션 모두 사용 가능) -   필요한 경우 사용
    testImplementation(libs.junit)
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")

    //  안드로이드 환경/UI 테스트용 (주로 :presentation, :app 모듈에서 사용)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation("io.mockk:mockk-android:1.13.10")
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    implementation(libs.androidx.activity.compose)
}