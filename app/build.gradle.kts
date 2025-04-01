import com.google.protobuf.gradle.proto

plugins {
    id("com.google.protobuf")
    id("com.android.library")
    id("maven-publish")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    kotlin("android")
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

    sourceSets {
        getByName("main") {
            java.srcDir("${layout.buildDirectory}/generated/source/proto/main/java")
            java.srcDir("${layout.buildDirectory}/generated/source/proto/main/kotlin")
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isJniDebuggable = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        create("staging") {
            isMinifyEnabled = false
            isJniDebuggable = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                artifact(tasks.named("bundleProtobufOutputs").get()) {
                    classifier = "protobuf"
                }

                groupId = "com.eka.records"
                artifactId = "eka-records"
                version = "3.2.3"
            }
        }
    }
    tasks.named("publishReleasePublicationToMavenLocal") {
        dependsOn(tasks.named("bundleReleaseAar"))
    }
}

tasks.register<Jar>("bundleProtobufOutputs") {
    from("${layout.buildDirectory}/generated/source/proto/main/java")
    from("${layout.buildDirectory}/generated/source/proto/main/kotlin")
    archiveClassifier.set("protobuf")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.20.1"
    }

    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("kotlin") {
                    option("lite")
                }
                create("java") {
                    option("lite")
                }
            }
        }
    }
}

dependencies {
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
    implementation(libs.eka.android.pdf)
    implementation("com.github.eka-care:eka-network-android:1.0.3") {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }
    api(libs.protobuf.kotlin.lite)
    api("com.google.protobuf:protobuf-javalite:4.26.1") {
        exclude(module = "protobuf-java")
    }
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
    implementation(libs.accompanist.permissions)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
}