# CuteReminder

Paper 1.26.2 — Kotlin — small fun plugin with jokes and reminders.

Built on the paper-kotlin template. Adds `/joke` alongside `/ping` — both backed by HikariCP (sqlite/mysql/pg).

```bash
gradle shadowJar
# → build/libs/template-plugin-1.0.0.jar
```

## Commands

- `/ping` — your latency (`template.ping`), `/ping <player>` (`template.ping.others`)
- `/joke` — random joke from official-joke-api (async, no DB)

## Config

`plugins/TemplatePlugin/config.yml` (sqlite by default):

```yaml
database:
  type: sqlite # sqlite | mysql | postgresql
  sqlite: { file: database.db }
ping:
  save-history: true
```

Change `database.type` → restart.

## Dev

```bash
nix develop        # JDK 21 + Gradle + ktlint + nixfmt + IDEA
gradle spotlessApply && nix fmt
gradle shadowJar
```

Paper & Spigot compatible. See `docs/` for DB and dev details.
