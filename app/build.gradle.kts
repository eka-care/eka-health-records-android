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
    compileSdk = 35

    defaultConfig {
        minSdk = 23
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
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
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
                version = "3.2.2"
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
    kapt(libs.room.compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.zelory.compressor)
    implementation(libs.google.gson)
    implementation("com.github.eka-care:eka-network-android:1.0.3") {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }
    api("com.google.protobuf:protobuf-javalite:4.26.1") {
        exclude(module = "protobuf-java")
    }
    implementation(libs.androidx.core.ktx)
    implementation(libs.google.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.urlconnection)
    implementation(libs.retrofit) {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }
    implementation(libs.retrofit.gson)
    implementation(libs.haroldadmin.networkresponseadapter)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
}