package com.example.template

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class PingCommand(private val plugin: TemplatePlugin?) :
    CommandExecutor,
    TabCompleter {

    constructor() : this(null)

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        when (args.size) {
            0 -> {
                if (sender is Player) {
                    val ping = sender.ping
                    sender.sendMessage("§aPong! §7Your ping: §e${ping}ms")
                    saveAsync(sender)
                } else {
                    sender.sendMessage("§cUsage: /ping <player> (console must specify player)")
                }
                return true
            }
            1 -> {
                val target = sender.server.getPlayer(args[0])
                if (target == null) {
                    sender.sendMessage("§cPlayer not found: ${args[0]}")
                    return true
                }
                if (!sender.hasPermission("template.ping.others") && sender != target) {
                    sender.sendMessage("§cNo permission to check others' ping.")
                    return true
                }
                val ping = target.ping
                sender.sendMessage("§a${target.name}'s ping: §e${ping}ms")
                saveAsync(target)
                return true
            }
            else -> {
                sender.sendMessage("§cUsage: /ping [player]")
                return true
            }
        }
    }

    private fun saveAsync(p: Player) {
        val pl = plugin ?: return
        if (!pl.config.getBoolean("ping.save-history", true)) return
        val db = pl.database
        if (!db.isConnected()) return
        val repo = pl.pingHistory
        val playerRepo = pl.playerData
        pl.server.asyncScheduler.runNow(pl) { _ ->
            repo.save(p.uniqueId, p.name, p.ping)
            playerRepo.upsert(p.uniqueId, p.name)
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>,
    ): List<String> {
        if (args.size == 1 && sender.hasPermission("template.ping.others")) {
            val prefix = args[0].lowercase()
            return sender.server.onlinePlayers
                .map { it.name }
                .filter { it.lowercase().startsWith(prefix) }
                .sorted()
        }
        return emptyList()
    }
}
