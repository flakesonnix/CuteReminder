# TemplatePlugin — Paper 1.26.2

Minimal Paper plugin with `/ping` command.

## Req
- JDK 21
- Gradle 8+ or `nix develop`

## Dev (nix)
```bash
nix develop
gradle build
# jar → build/libs/template-plugin-1.0.0.jar
```

## Dev (without nix)
```bash
gradle build
```

Drop jar into `plugins/` and restart.

## Command
- `/ping` → own ping
- `/ping <player>` → others' ping (perm `template.ping.others`)
- perm `template.ping` default true

## Notes
- `src/main/resources/plugin.yml` + `paper-plugin.yml` — Paper prefers `paper-plugin.yml`.
- Version `1.26.2-R0.1-SNAPSHOT` in `build.gradle.kts`. If Paper 1.26.2 not yet on repo, change to latest (e.g. `1.21.10-R0.1-SNAPSHOT`).
- Built with `api-version: '1.21'` — bump if Paper 1.26 requires newer.
