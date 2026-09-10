package com.example.template.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.sql.Connection
import java.sql.SQLException
import java.util.logging.Level

/**
 * HikariCP-backed DB. Supports sqlite (default), mysql, postgresql.
 * Config-driven via plugin config.yml → database.*
 */
class Database(private val plugin: JavaPlugin) {

    private var ds: HikariDataSource? = null

    fun connect() {
        if (ds?.isClosed == false) return

        val type = plugin.config.getString("database.type", "sqlite")!!.lowercase()
        val cfg = HikariConfig().apply {
            poolName = "TemplatePlugin-Hikari"
            maximumPoolSize = plugin.config.getInt("database.pool.maximum-pool-size", 10)
            minimumIdle = plugin.config.getInt("database.pool.minimum-idle", 2)
            connectionTimeout = plugin.config.getLong("database.pool.connection-timeout", 30000)
            idleTimeout = plugin.config.getLong("database.pool.idle-timeout", 600000)
            maxLifetime = plugin.config.getLong("database.pool.max-lifetime", 1800000)
            leakDetectionThreshold = 60000
        }

        when (type) {
            "mysql" -> configureMySql(cfg)
            "postgresql", "postgres", "pgsql" -> configurePostgres(cfg)
            "sqlite" -> configureSqlite(cfg)
            else -> {
                plugin.logger.warning("Unknown database.type '$type' → fallback to sqlite")
                configureSqlite(cfg)
            }
        }

        cfg.addDataSourceProperty("cachePrepStmts", "true")
        cfg.addDataSourceProperty("prepStmtCacheSize", "250")
        cfg.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")

        ds = HikariDataSource(cfg)
        plugin.logger.info("DB connected [$type] pool=${cfg.maximumPoolSize}")
    }

    private fun configureSqlite(cfg: HikariConfig) {
        val fileName = plugin.config.getString("database.sqlite.file", "database.db")!!
        val file = File(plugin.dataFolder, fileName)
        file.parentFile?.mkdirs()
        val url = "jdbc:sqlite:${file.absolutePath}"
        cfg.jdbcUrl = url
        cfg.driverClassName = "org.sqlite.JDBC"
        cfg.maximumPoolSize = 1
        cfg.minimumIdle = 1
        cfg.connectionTestQuery = "SELECT 1"
        plugin.logger.info("SQLite URL: $url")
    }

    private fun configureMySql(cfg: HikariConfig) {
        val host = plugin.config.getString("database.mysql.host", "localhost")!!
        val port = plugin.config.getInt("database.mysql.port", 3306)
        val db = plugin.config.getString("database.mysql.database", "minecraft")!!
        val user = plugin.config.getString("database.mysql.user", "root")!!
        val pass = plugin.config.getString("database.mysql.password", "")!!
        val params = plugin.config.getString("database.mysql.params", "useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=utf8mb4")!!
        val url = "jdbc:mysql://$host:$port/$db?$params"
        cfg.jdbcUrl = url
        cfg.username = user
        cfg.password = pass
        cfg.driverClassName = "com.mysql.cj.jdbc.Driver"
    }

    private fun configurePostgres(cfg: HikariConfig) {
        val host = plugin.config.getString("database.postgresql.host", "localhost")!!
        val port = plugin.config.getInt("database.postgresql.port", 5432)
        val db = plugin.config.getString("database.postgresql.database", "minecraft")!!
        val user = plugin.config.getString("database.postgresql.user", "postgres")!!
        val pass = plugin.config.getString("database.postgresql.password", "")!!
        val params = plugin.config.getString("database.postgresql.params", "sslmode=disable")!!
        val url = "jdbc:postgresql://$host:$port/$db?$params"
        cfg.jdbcUrl = url
        cfg.username = user
        cfg.password = pass
        cfg.driverClassName = "org.postgresql.Driver"
    }

    fun migrate() {
        val ai = if (isSqlite()) "AUTOINCREMENT" else "AUTO_INCREMENT"
        val ddls = arrayOf(
            """
            CREATE TABLE IF NOT EXISTS ping_history (
              id INTEGER PRIMARY KEY $ai,
              player_uuid VARCHAR(36) NOT NULL,
              player_name VARCHAR(16) NOT NULL,
              ping INT NOT NULL,
              recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS example_players (
              uuid VARCHAR(36) PRIMARY KEY,
              name VARCHAR(16) NOT NULL,
              first_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
              last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """.trimIndent(),
            if (isSqlite()) "CREATE INDEX IF NOT EXISTS idx_ping_uuid ON ping_history(player_uuid)"
            else "CREATE INDEX idx_ping_uuid ON ping_history(player_uuid)"
        )

        try {
            getConnection().use { c ->
                c.createStatement().use { s ->
                    for (ddl in ddls) {
                        try {
                            s.execute(ddl)
                        } catch (e: SQLException) {
                            val msg = e.message ?: ""
                            if (!msg.contains("already exists") && !msg.contains("Duplicate")) throw e
                        }
                    }
                }
            }
            plugin.logger.info("DB migrate done")
        } catch (e: SQLException) {
            plugin.logger.log(Level.SEVERE, "DB migrate failed", e)
        }
    }

    @Throws(SQLException::class)
    fun getConnection(): Connection {
        val dataSource = ds ?: throw SQLException("DataSource not initialized — call connect()")
        return dataSource.connection
    }

    fun isConnected(): Boolean = ds?.isClosed == false

    fun isSqlite(): Boolean = ds?.jdbcUrl?.startsWith("jdbc:sqlite") == true

    fun close() {
        val d = ds
        if (d != null && !d.isClosed) {
            d.close()
            plugin.logger.info("DB pool closed")
        }
    }

    val dataSource: HikariDataSource? get() = ds
}
