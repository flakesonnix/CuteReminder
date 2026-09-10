package com.example.template.db

import java.sql.SQLException
import java.util.UUID
import java.util.logging.Level
import java.util.logging.Logger

class PingHistoryRepository(
    private val db: Database,
    private val log: Logger,
) {

    data class Entry(val uuid: UUID, val name: String, val ping: Int, val at: String)

    fun save(uuid: UUID, name: String, ping: Int) {
        val sql = "INSERT INTO ping_history(player_uuid, player_name, ping) VALUES (?,?,?)"
        try {
            db.getConnection().use { c ->
                c.prepareStatement(sql).use { ps ->
                    ps.setString(1, uuid.toString())
                    ps.setString(2, name)
                    ps.setInt(3, ping)
                    ps.executeUpdate()
                }
            }
        } catch (e: SQLException) {
            log.log(Level.WARNING, "save ping failed", e)
        }
    }

    fun recent(uuid: UUID, limit: Int): List<Entry> {
        val sql = "SELECT player_uuid, player_name, ping, recorded_at FROM ping_history WHERE player_uuid=? ORDER BY recorded_at DESC LIMIT ?"
        val out = mutableListOf<Entry>()
        try {
            db.getConnection().use { c ->
                c.prepareStatement(sql).use { ps ->
                    ps.setString(1, uuid.toString())
                    ps.setInt(2, limit)
                    ps.executeQuery().use { rs ->
                        while (rs.next()) {
                            out += Entry(
                                UUID.fromString(rs.getString(1)),
                                rs.getString(2),
                                rs.getInt(3),
                                rs.getString(4),
                            )
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            log.log(Level.WARNING, "recent ping query failed", e)
        }
        return out
    }

    fun average(uuid: UUID, lastN: Int): Double {
        val sql = "SELECT AVG(ping) FROM (SELECT ping FROM ping_history WHERE player_uuid=? ORDER BY recorded_at DESC LIMIT ?)"
        try {
            db.getConnection().use { c ->
                c.prepareStatement(sql).use { ps ->
                    ps.setString(1, uuid.toString())
                    ps.setInt(2, lastN)
                    ps.executeQuery().use { rs ->
                        if (rs.next()) return rs.getDouble(1)
                    }
                }
            }
        } catch (e: SQLException) {
            log.log(Level.WARNING, "avg ping failed", e)
        }
        return -1.0
    }
}
