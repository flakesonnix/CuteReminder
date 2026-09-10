package com.example.template.db

import java.sql.SQLException
import java.util.UUID
import java.util.logging.Level
import java.util.logging.Logger

class PlayerDataRepository(
    private val db: Database,
    private val log: Logger
) {

    fun upsert(uuid: UUID, name: String) {
        val sqlite = db.isSqlite()
        val sql = if (sqlite) {
            "INSERT INTO example_players(uuid, name, last_seen) VALUES (?,?,CURRENT_TIMESTAMP) ON CONFLICT(uuid) DO UPDATE SET name=excluded.name, last_seen=CURRENT_TIMESTAMP"
        } else {
            "INSERT INTO example_players(uuid, name) VALUES (?,?) ON DUPLICATE KEY UPDATE name=VALUES(name), last_seen=CURRENT_TIMESTAMP"
        }
        try {
            db.getConnection().use { c ->
                c.prepareStatement(sql).use { ps ->
                    ps.setString(1, uuid.toString())
                    ps.setString(2, name)
                    ps.executeUpdate()
                }
            }
        } catch (e: SQLException) {
            log.log(Level.WARNING, "upsert player failed", e)
        }
    }
}
