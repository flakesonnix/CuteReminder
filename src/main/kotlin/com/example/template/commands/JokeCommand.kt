package com.example.template.commands

import com.example.template.api.JokeApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class JokeCommand(
    private val plugin: Plugin,
    private val jokeApiClient: JokeApiClient,
) : CommandExecutor {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Only players can use this command.")
            return true
        }

        sender.sendMessage("§7Fetching a joke...")

        scope.launch {
            val joke = jokeApiClient.fetchRandomJoke()
            plugin.server.scheduler.runTask(
                plugin,
                Runnable {
                    if (joke == null) {
                        sender.sendMessage("§cCould not fetch a joke. Try again later.")
                        return@Runnable
                    }
                    sender.sendMessage("§e${joke.setup}")
                    sender.sendMessage("§a${joke.punchline}")
                },
            )
        }
        return true
    }
}
