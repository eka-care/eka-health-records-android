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
        minSdk = 24
        targetSdk = 34
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
            version = "1.0.7"

            artifact("../app/build/outputs/aar/app-release.aar")
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    kapt(libs.room.compiler)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.appcompat)
    implementation(libs.android.material)
    implementation(libs.androidx.compose.material3)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation (libs.compose.shimmer)
    implementation(libs.androidx.activity.compose)
    implementation(libs.coil.compose)
    implementation(libs.play.services.mlkit.document.scanner)
    implementation(libs.zelory.compressor)
    implementation(libs.google.gson)
    implementation("com.github.Saroj-EkaCare:Jet-Pdf-Reader:1.1.1")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}