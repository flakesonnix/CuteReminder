package com.example.template

import com.example.template.api.JokeApiClient
import com.example.template.commands.JokeCommand
import com.example.template.db.Database
import com.example.template.db.PingHistoryRepository
import com.example.template.db.PlayerDataRepository
import org.bukkit.plugin.java.JavaPlugin

class TemplatePlugin : JavaPlugin() {

    lateinit var database: Database
        private set
    lateinit var pingHistory: PingHistoryRepository
        private set
    lateinit var playerData: PlayerDataRepository
        private set
    private lateinit var jokeApiClient: JokeApiClient

    override fun onEnable() {
        saveDefaultConfig()

        // --- DB ---
        database = Database(this)
        try {
            database.connect()
            database.migrate()
        } catch (e: Exception) {
            logger.severe("DB init failed — plugin still runs but DB disabled: ${e.message}")
            e.printStackTrace()
        }
        pingHistory = PingHistoryRepository(database, logger)
        playerData = PlayerDataRepository(database, logger)

        jokeApiClient = JokeApiClient()

        // --- listener ---
        server.pluginManager.registerEvents(PlayerJoinListener(this), this)
        getCommand("joke")?.setExecutor(JokeCommand(this, jokeApiClient))
        // --- cmd ---
        val cmd = getCommand("ping")
        if (cmd != null) {
            val executor = PingCommand(this)
            cmd.setExecutor(executor)
            cmd.tabCompleter = executor
        } else {
            logger.warning("ping command not registered — check plugin.yml / paper-plugin.yml")
        }
        logger.info("TemplatePlugin enabled (DB=${if (database.isConnected()) "ok" else "off"})")
    }

    override fun onDisable() {
        if (::jokeApiClient.isInitialized) jokeApiClient.close()
        if (::database.isInitialized) database.close()
        logger.info("TemplatePlugin disabled")
    }
}
