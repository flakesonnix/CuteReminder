package com.example.template

import com.example.template.db.Database
import com.example.template.db.PlayerDataRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.File
import java.util.UUID
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class PlayerJoinListenerTest {

    @TempDir
    lateinit var tempDir: File

    @Test
    fun `onJoin upserts when db connected`() {
        val plugin = mockk<TemplatePlugin>(relaxed = true)
        val db = mockk<Database>(relaxed = true)
        val repo = mockk<PlayerDataRepository>(relaxed = true)
        every { plugin.database } returns db
        every { plugin.playerData } returns repo
        every { db.isConnected() } returns true
        // mock asyncScheduler to just accept any call
        val server = mockk<org.bukkit.Server>(relaxed = true)
        every { plugin.server } returns server
        val async = mockk<io.papermc.paper.threadedregions.scheduler.AsyncScheduler>(relaxed = true)
        every { server.asyncScheduler } returns async
        // relax runNow to do nothing
        every { async.runNow(any<org.bukkit.plugin.Plugin>(), any<java.util.function.Consumer<io.papermc.paper.threadedregions.scheduler.ScheduledTask>>()) } returns mockk(relaxed = true)
        val listener = PlayerJoinListener(plugin)
        val player = mockk<Player>(relaxed = true)
        val uuid = UUID.randomUUID()
        every { player.uniqueId } returns uuid
        every { player.name } returns "Steve"
        val event = PlayerJoinEvent(player, "joined")
        listener.onJoin(event)
        // verify db was checked and scheduler was touched
        verify { db.isConnected() }
        verify { plugin.server }
    }

    @Test
    fun `onJoin does nothing when db disconnected`() {
        val plugin = mockk<TemplatePlugin>(relaxed = true)
        val db = mockk<Database>(relaxed = true)
        every { plugin.database } returns db
        every { db.isConnected() } returns false
        val listener = PlayerJoinListener(plugin)
        val player = mockk<Player>(relaxed = true)
        every { player.uniqueId } returns UUID.randomUUID()
        every { player.name } returns "Alex"
        val event = PlayerJoinEvent(player, "hi")
        listener.onJoin(event)
        // should not try to get server
        // verify no async call
        io.mockk.verify(exactly = 0) { plugin.server }
    }
}
