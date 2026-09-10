# TemplatePlugin — Paper 1.26.2 — Kotlin

Minimal Paper (Kotlin) plugin with `/ping` + DB layer (HikariCP + sqlite/mysql/postgres).

## Req
- JDK 21
- Gradle 8+ or `nix develop` (provides JDK 21 + Gradle 8.14 + Kotlin 2.0.21)
- Kotlin 2.0.21 (via Gradle)
- DB: sqlite default (no setup) or mysql/postgres via docker

## Quick start
```bash
nix develop
gradle shadowJar
# jar → build/libs/template-plugin-1.0.0.jar
docker compose up -d # optional — only if you switch to mysql/postgres
```
Drop jar into `plugins/` → restart. Logs: `DB connected [sqlite]` + `DB migrate done`.

## Commands
- `/ping` → own ping (+ saves to DB if `ping.save-history: true`)
- `/ping <player>` → others' ping (perm `template.ping.others`)
- perm `template.ping` default true

## Database

Default `config.yml` → sqlite `plugins/TemplatePlugin/database.db` — zero setup.

Switch:
```yaml
database:
  type: mysql # or postgresql
  mysql: { host: localhost, port: 3306, database: minecraft, user: root, password: change_me }
```

Pool: `database.pool.*` (HikariCP). Docs → [`docs/DATABASE.md`](docs/DATABASE.md).

Arch (Kotlin):
- `src/main/kotlin/com/example/template/db/Database.kt` — Hikari wrapper + migrate
- `src/main/kotlin/com/example/template/db/PingHistoryRepository.kt` — example DAO
- `src/main/kotlin/com/example/template/db/PlayerDataRepository.kt` — upsert example
- `src/main/kotlin/com/example/template/TemplatePlugin.kt` — main, wires DB
- Async: `server.asyncScheduler.runNow(plugin) { repo.save(...) }` — never block main.
```kotlin
// example DAO usage (Kotlin)
plugin.server.asyncScheduler.runNow(plugin) {
    pingHistory.save(player.uniqueId, player.name, player.ping)
    val recent = pingHistory.recent(player.uniqueId, 10)
}
```

Dev DBs: `docker-compose.yml` (mysql + postgres commented out). `docker compose up -d`.

Slim jar: remove unwanted drivers in `build.gradle.kts` (`mysql-connector-j` / `postgresql`).

## Dev
```bash
nix develop      # jdk21 + gradle8 + jetbrains.idea (native) + formatters
gradle shadowJar # fat jar (manual) →  build/libs/*.jar
gradle build     # also builds shadowJar
gradle spotlessApply # Kotlin fmt (ktlint 1.5.0 native)
gradle spotlessCheck # Kotlin fmt check
nix fmt          # Nix fmt (nixfmt native)
nix run .#idea   # JetBrains IDEA (jetbrains.idea)
gradle idea      # gen .idea/.iml

nix build        # via flake → result/*.jar
./gradlew -PpaperVersion=1.26.2-R0.1-SNAPSHOT shadowJar # use once Paper 1.26.2 published
```

## Formatter
- **Kotlin** → `com.diffplug.spotless` + `ktlint 1.5.0` (Kotlin native style, `KOTLIN_OFFICIAL`). Config in `build.gradle.kts` + `.editorconfig` (4 spaces, 120+). Run `gradle spotlessApply`.
- **Nix** → `nixfmt` (native Rust, `formatter = pkgs.nixfmt` in `flake.nix`). Run `nix fmt`.
- **IDEA** → `.idea/codeStyles/Project.xml` = `KOTLIN_OFFICIAL`, `gradle.xml`/`misc.xml` JDK21. `.editorconfig` synced for both.
- **Why native?** Nix fmt = native (`nixfmt`), Kotlin fmt = `ktlint` native binary via `pkgs.ktlint` + Spotless JVM wrapper (JVM required for Paper API; plugin itself must be Kotlin-JVM, not Kotlin/Native — Paper is JVM-only).

## Notes
- `src/main/resources/plugin.yml` + `paper-plugin.yml` — Paper prefers `paper-plugin.yml`.
- `build.gradle.kts` default `paperVersion=1.21.10-R0.1-SNAPSHOT` (1.26.2 not yet on repo). Pass `-PpaperVersion=1.26.2-R0.1-SNAPSHOT` once published.
- `api-version: '1.21'` — bump if Paper 1.26 requires newer.
- Fat jar via manual `shadowJar` task (no relocation). Add `org.gradle.shadow` 8.3.x if you need relocate `com.zaxxer.hikari → com.example.template.libs.hikari`.
- Kotlin `jvmTarget = 21`, toolchain 21.
