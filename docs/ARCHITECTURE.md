# Architecture — CuteReminder

Fork of paper-kotlin-template with fun commands.

```
Paper
  └─ TemplatePlugin (CuteReminder)
       ├─ Database — same as template (HikariCP)
       ├─ PingCommand — /ping
       └─ JokeCommand — /joke → JokeApiClient (Ktor CIO) → official-joke-api
```

`Joke` model via kotlinx.serialization. `JokeApiClient` is closed onDisable. `Reminder` model is stub for future `/remind` feature — not wired yet.

Keep template structure; add new commands under `commands/` and clients under `api/`.
