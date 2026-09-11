# Database — CuteReminder

Same HikariCP setup as template — sqlite default. `Database` handles pool and `migrate()` for `ping_history` and `example_players`.

Add new tables for reminders in `Database.migrate()` when you implement `/remind` (see `model/Reminder.kt` stub).
