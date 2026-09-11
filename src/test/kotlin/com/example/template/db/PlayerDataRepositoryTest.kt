package com.example.template.db

import io.mockk.every
import io.mockk.mockk
import java.io.File
import java.util.UUID
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class PlayerDataRepositoryTest {

    @TempDir
    lateinit var tempDir: File

    private fun db(): Database {
        val p = mockk<JavaPlugin>(relaxed = true)
        val c = YamlConfiguration()
        c.set("database.type", "sqlite")
        c.set("database.sqlite.file", "test.db")
        c.set("database.pool.maximum-pool-size", 1)
        every { p.config } returns c
        every { p.dataFolder } returns tempDir
        every { p.logger } returns mockk(relaxed = true)
        val db = Database(p)
        db.connect()
        db.migrate()
        return db
    }

    @Test
    fun `upsert inserts and updates`() {
        val db = db()
        val repo = PlayerDataRepository(db, mockk(relaxed = true))
        val uuid = UUID.randomUUID()
        assertDoesNotThrow { repo.upsert(uuid, "Steve") }
        assertDoesNotThrow { repo.upsert(uuid, "Steve2") }
        // verify via raw query
        db.getConnection().use { c ->
            c.prepareStatement("SELECT name FROM example_players WHERE uuid=?").use { ps ->
                ps.setString(1, uuid.toString())
                ps.executeQuery().use { rs ->
                    assert(rs.next())
                    assert(rs.getString(1) == "Steve2")
                }
            }
        }
        db.close()
    }

    @Test
    fun `upsert different uuids`() {
        val db = db()
        val repo = PlayerDataRepository(db, mockk(relaxed = true))
        repo.upsert(UUID.randomUUID(), "A")
        repo.upsert(UUID.randomUUID(), "B")
        db.getConnection().use { c ->
            c.createStatement().use { st ->
                st.executeQuery("SELECT COUNT(*) FROM example_players").use { rs ->
                    rs.next()
                    assert(rs.getInt(1) >= 2)
                }
            }
        }
        db.close()
    }
}
