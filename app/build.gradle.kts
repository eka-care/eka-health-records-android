plugins {
    id("com.android.library")
    id("maven-publish")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "eka.care.documents"
    compileSdk = 34

    defaultConfig {
        minSdk = 23
        targetSdk = 34
        multiDexEnabled = true
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
    kotlin {
        jvmToolchain(17)
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
}

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "com.eka.records"
            artifactId = "eka-records"
            version = "1.1.8"

            artifact("../app/build/outputs/aar/app-release.aar")
        }
    }
}

dependencies {
    implementation(project(":protobuf"))
    implementation(libs.androidx.work.runtime.ktx)
    implementation(platform(libs.androidx.compose.bom))
    kapt(libs.room.compiler)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material3)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation (libs.compose.shimmer)
    implementation(libs.androidx.activity.compose)
    implementation(libs.coil.compose)
    api(libs.play.services.mlkit.document.scanner)
    implementation(libs.zelory.compressor)
    implementation(libs.google.gson)
    implementation("com.github.Saroj-EkaCare:Jet-Pdf-Reader:1.1.4")
    implementation("com.github.eka-care:eka-network-android:1.0.1") {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }
    implementation(libs.protobuf.kotlin.lite)
    implementation("com.google.protobuf:protobuf-javalite:4.26.1") {
        exclude(module = "protobuf-java")
    }
//    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation(libs.androidx.core.ktx)
    implementation(libs.google.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.urlconnection)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.retrofit) {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }
    implementation(libs.retrofit.gson)
    implementation(libs.retrofit.scalars)
    implementation(libs.haroldadmin.networkresponseadapter)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.google.accompanist.pager)
    implementation(libs.google.accompanist.pager.indicators)
}