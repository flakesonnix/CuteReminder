plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // Paper 1.26.2 — if not yet published, fallback to latest: 1.21.10-R0.1-SNAPSHOT
    compileOnly("io.papermc.paper:paper-api:1.26.2-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.processResources {
    filteringCharset = "UTF-8"
}

tasks.jar {
    archiveBaseName.set("template-plugin")
}
