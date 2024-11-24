plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("com.google.protobuf")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

java {
    withSourcesJar() // Optional: Include only the source JAR if needed
}
tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
tasks.register<Jar>("bundleProtobufOutputs") {
    from("${project(":protobuf").buildDir}/generated/source/proto/main/java")
    from("${project(":protobuf").buildDir}/generated/source/proto/main/kotlin")
    archiveClassifier.set("protobuf")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.20.1"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                named("java") {
                    option("lite")
                }
                create("kotlin") {
                    option("lite")
                }
            }
        }
    }
}

tasks.withType<Jar> {
    from("${buildDir}/generated/source/proto/main/java")
    from("${buildDir}/generated/source/proto/main/kotlin")
}

dependencies {
    api(libs.protobuf.kotlin.lite)
    api(libs.protobuf.javalite)
}