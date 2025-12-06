plugins {
    kotlin("jvm") version "2.2.21"
    application
}

group = "fan.dang.wcfont"
version = "1.0.0"

repositories {
    mavenCentral()
}

val vertxVersion = "5.0.5"
val kotlinCoroutinesVersion = "1.10.2"
val jacksonVersion = "2.18.2"

dependencies {
    // Vert.x
    implementation("io.vertx:vertx-core:$vertxVersion")
    implementation("io.vertx:vertx-web:$vertxVersion")
    implementation("io.vertx:vertx-lang-kotlin:$vertxVersion")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:$vertxVersion")
    implementation("io.vertx:vertx-config:$vertxVersion")
    implementation("io.vertx:vertx-jdbc-client:$vertxVersion")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")

    // Jackson for JSON
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")

    // JWT for authentication
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // BCrypt for password hashing
    implementation("at.favre.lib:bcrypt:0.10.2")

    // Markdown parsing
    implementation("com.vladsch.flexmark:flexmark-all:0.64.8")

    // OpenCC for Chinese conversion (JNA)
    implementation("net.java.dev.jna:jna:5.15.0")

    // sfntly for font subsetting
    implementation("fr.opensagres.xdocreport.sfntly:sfntly:1.0.0")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // Testing
    testImplementation("io.vertx:vertx-junit5:$vertxVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("fan.dang.app.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "fan.dang.app.MainKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
}
