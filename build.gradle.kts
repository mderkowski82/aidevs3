plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.allopen") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    id("io.quarkus")
}

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-mutiny")
    implementation("io.quarkus:quarkus-rest-client")
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkiverse.openapi.generator:quarkus-openapi-generator:2.5.0")
    implementation("io.quarkus:quarkus-vertx")
//    implementation("io.quarkiverse.langchain4j:quarkus-langchain4j-easy-rag:0.20.3")
//    implementation("io.quarkiverse.langchain4j:quarkus-langchain4j-openai:0.20.3")
    implementation("io.quarkus:quarkus-rest-client-kotlin-serialization")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-hibernate-reactive-panache-kotlin")
    implementation("io.quarkus:quarkus-smallrye-context-propagation")
    implementation("io.quarkus:quarkus-rest-kotlin-serialization")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-oidc-client")
// https://mvnrepository.com/artifact/com.aallam.openai/openai-client
    implementation("com.aallam.openai:openai-client:4.0.0-beta01")
// https://mvnrepository.com/artifact/com.squareup.okio/okio
    implementation("com.squareup.okio:okio:3.9.1")

// https://mvnrepository.com/artifact/io.ktor/ktor-client-okhttp-jvm
    runtimeOnly("io.ktor:ktor-client-okhttp-jvm:3.0.1")
// https://mvnrepository.com/artifact/io.ktor/ktor-client-core
//    implementation("io.ktor:ktor-client-okhttp:3.0.1")
//    implementation("io.ktor:ktor-client-core:3.0.1")
// https://mvnrepository.com/artifact/io.ktor/ktor-client-core
//    runtimeOnly("io.ktor:ktor-client-core:3.0.1")


    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("org.mockito:mockito-core")
    testImplementation("io.rest-assured:rest-assured")
}

group = "pl.npesystem"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_21.toString()
    kotlinOptions.javaParameters = true
}
