plugins {
    // Kotlin — template now fully Kotlin
    kotlin("jvm") version "2.0.21"
    // Shadow removed — manual fatJar used to avoid ASM 65 issue (shadow 8.1.1 can't read Java 21).
    // If you want relocation, add org.gradle.shadow 8.3.x + re-enable relocate block below.
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

val paperVersion = findProperty("paperVersion") as String? ?: "1.21.10-R0.1-SNAPSHOT" // target 1.26.2 when released → ./gradlew -PpaperVersion=1.26.2-R0.1-SNAPSHOT build

dependencies {
    // Paper 1.26.2 target — defaults to 1.21.10 until 1.26.2 hits repo.papermc.io (API compat same)
    compileOnly("io.papermc.paper:paper-api:$paperVersion")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // --- DB ---
    implementation("com.zaxxer:HikariCP:6.2.1")
    implementation("org.xerial:sqlite-jdbc:3.47.1.0")
    // mysql + postgres drivers — remove what you don't need to slim jar
    implementation("com.mysql:mysql-connector-j:9.2.0")
    implementation("org.postgresql:postgresql:42.7.5")
    // slf4j needed by HikariCP (Paper provides it but include for shade)
    implementation("org.slf4j:slf4j-api:2.0.16")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

tasks.processResources {
    filteringCharset = "UTF-8"
}

tasks.jar {
    archiveBaseName.set("template-plugin")
}

// Manual fatJar — bundles runtimeClasspath (HikariCP + drivers + kotlin stdlib) without shadow ASM.
// No relocation (add shadow 8.3.x if you need relocate to avoid lib conflicts).
val shadowJar by tasks.registering(Jar::class) {
    archiveBaseName.set("template-plugin")
    archiveClassifier.set("")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    })
    // merge service files (e.g., sqlite jdbc) — naive: exclude duplicates already
}

tasks.build {
    dependsOn(shadowJar)
}
