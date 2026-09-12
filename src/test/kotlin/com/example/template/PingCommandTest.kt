package com.example.template

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bukkit.Server
import org.bukkit.command.Command
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PingCommandTest {
    @Test
    fun `ping self sends pong`() {
        val plugin = mockk<TemplatePlugin>(relaxed = true)
        val config = YamlConfiguration()
        config.set("ping.save-history", false)
        every { plugin.config } returns config
        every { plugin.logger } returns mockk(relaxed = true)
        val cmd = PingCommand(plugin)
        val player = mockk<Player>(relaxed = true)
        every { player.ping } returns 42
        every { player.name } returns "Test"
        every { player.uniqueId } returns java.util.UUID.randomUUID()
        val server = mockk<Server>(relaxed = true)
        every { player.server } returns server
        val sender = player
        val command = mockk<Command>(relaxed = true)
        val result = cmd.onCommand(sender, command, "ping", arrayOf())
        assertTrue(result)
        verify { sender.sendMessage(match<String> { it.contains("42") }) }
    }

    @Test
    fun `ping others tabComplete empty when no perm`() {
        val plugin = mockk<TemplatePlugin>(relaxed = true)
        val cmd = PingCommand(plugin)
        val sender = mockk<Player>(relaxed = true)
        every { sender.hasPermission("template.ping.others") } returns false
        val res = cmd.onTabComplete(sender, mockk(relaxed = true), "ping", arrayOf("a"))
        assertTrue(res.isEmpty())
    }
}
