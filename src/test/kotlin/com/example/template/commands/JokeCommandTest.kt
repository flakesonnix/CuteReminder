package com.example.template.commands

import com.example.template.api.Joke
import com.example.template.api.JokeApiClient
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bukkit.Server
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitScheduler
import org.junit.jupiter.api.Test

class JokeCommandTest {

    private fun mockServer(syncRunsImmediately: Boolean = true): Pair<JavaPlugin, Server> {
        val plugin = mockk<JavaPlugin>(relaxed = true)
        val server = mockk<Server>(relaxed = true)
        val scheduler = mockk<BukkitScheduler>(relaxed = true)
        every { plugin.server } returns server
        every { server.scheduler } returns scheduler
        if (syncRunsImmediately) {
            every { scheduler.runTask(any(), any<Runnable>()) } answers {
                secondArg<Runnable>().run()
                mockk(relaxed = true)
            }
        }
        every { plugin.getResource(any()) } returns null
        every { plugin.config } returns YamlConfiguration()
        return plugin to server
    }

    @Test
    fun `non-player sender gets message`() {
        val (plugin, _) = mockServer()
        val jokeClient = mockk<JokeApiClient>(relaxed = true)
        val cmd = JokeCommand(plugin, jokeClient)
        val sender = mockk<org.bukkit.command.CommandSender>(relaxed = true)
        // sender is not a Player
        val result = cmd.onCommand(sender, mockk(relaxed = true), "joke", arrayOf())
        assert(result)
        verify { sender.sendMessage("Only players can use this command.") }
    }

    @Test
    fun `player gets fetching message`() {
        val (plugin, server) = mockServer()
        val jokeClient = mockk<JokeApiClient>(relaxed = true)
        coEvery { jokeClient.fetchRandomJoke() } returns Joke(1, "general", "S", "P")
        val cmd = JokeCommand(plugin, jokeClient)
        val player = mockk<Player>(relaxed = true)
        every { player.server } returns server
        val result = cmd.onCommand(player, mockk(relaxed = true), "joke", arrayOf())
        assert(result)
        verify { player.sendMessage("§7Fetching a joke...") }
        // give coroutine time to run
        Thread.sleep(300)
        // verify joke messages sent via scheduler
        verify(timeout = 1000) { player.sendMessage("§eS") }
        verify(timeout = 1000) { player.sendMessage("§aP") }
    }

    @Test
    fun `player gets error when joke null`() {
        val (plugin, server) = mockServer()
        val jokeClient = mockk<JokeApiClient>(relaxed = true)
        coEvery { jokeClient.fetchRandomJoke() } returns null
        val cmd = JokeCommand(plugin, jokeClient)
        val player = mockk<Player>(relaxed = true)
        every { player.server } returns server
        cmd.onCommand(player, mockk(relaxed = true), "joke", arrayOf())
        Thread.sleep(300)
        verify(timeout = 1000) { player.sendMessage("§cCould not fetch a joke. Try again later.") }
    }
}
