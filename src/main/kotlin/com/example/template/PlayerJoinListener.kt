package com.example.template

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener(private val plugin: TemplatePlugin) : Listener {

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val p = e.player
        if (!plugin.database.isConnected()) return
        plugin.server.asyncScheduler.runNow(plugin) { _ ->
            plugin.playerData.upsert(p.uniqueId, p.name)
        }
    }
}
