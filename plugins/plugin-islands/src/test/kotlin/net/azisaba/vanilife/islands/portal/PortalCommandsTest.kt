package net.azisaba.vanilife.islands.portal

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.slot
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.inventory.ItemStack

class PortalCommandsTest : StringSpec({
    beforeTest { try { mockkStatic(Bukkit::class); every { Bukkit.getItemFactory() } returns mockk(relaxed = true) } catch (_: Exception) {} }
    afterTest { clearAllMocks(); try { unmockkStatic(Bukkit::class) } catch (_: Exception) {} ; try { stopKoin() } catch (_: Exception) {} }

    "list subcommand should send portal list to sender when permitted" {
        runTest {
            val mockRepo = mockk<PortalRepository>(relaxed = true)
            val mockPlugin = mockk<org.bukkit.plugin.Plugin>(relaxed = true)
            every { mockPlugin.name } returns "vanilife"
            val mockIslandRepo = mockk<net.azisaba.vanilife.islands.storage.IslandRepository>(relaxed = true)
            val mockSpawner = mockk<HologramSpawner>(relaxed = true)

            // provide manager with a sample portal
            val p = Portal(
                id = 1L,
                ownerUuid = java.util.UUID.randomUUID(),
                originWorldName = "w",
                originMin = io.papermc.paper.math.Position.block(0, 60, 0),
                originMax = io.papermc.paper.math.Position.block(0, 60, 0),
                orientation = net.azisaba.vanilife.islands.portal.finder.DetectedPortal.Orientation.XY,
                innerWidth = 1,
                innerHeight = 1,
                resourceWorldName = "r",
                resourceMin = io.papermc.paper.math.Position.block(0, 60, 0),
                resourceMax = io.papermc.paper.math.Position.block(0, 60, 0),
                hologramUuid = null,
                createdAt = 0L,
                active = true,
            )

            val manager = PortalManager(mockPlugin, mockRepo, mockIslandRepo, this, mockSpawner)

            // stub repo to return our single portal and load into manager
            coEvery { mockRepo.findActive() } returns listOf(p)

            startKoin { modules(module { single<PortalManager> { manager }; single<PortalRepository> { mockRepo }; single<org.bukkit.plugin.Plugin> { mockPlugin } }) }

            val cmd = PortalCommands()

            // load portals into manager
            manager.loadAll()
            this.testScheduler.advanceUntilIdle()

            val sender = mockk<CommandSender>(relaxed = true)
            every { sender.hasPermission("vanilife.portal.list") } returns true
            every { sender.isOp } returns false

            val command = mockk<Command>(relaxed = true)

            val result = cmd.onCommand(sender, command, "portal", arrayOf("list"))
            result shouldBe true

            // verify sender received at least the header message
            verify { sender.sendMessage(any<net.kyori.adventure.text.Component>()) }
        }
    }

    "unlink without args by owning player should remove portal" {
        runTest {
            val mockRepo = mockk<PortalRepository>(relaxed = true)
            val mockPlugin = mockk<org.bukkit.plugin.Plugin>(relaxed = true)
            every { mockPlugin.name } returns "vanilife"
            val mockIslandRepo = mockk<net.azisaba.vanilife.islands.storage.IslandRepository>(relaxed = true)
            val mockSpawner = mockk<HologramSpawner>(relaxed = true)

            val ownerId = java.util.UUID.randomUUID()
            val p = Portal(
                id = 2L,
                ownerUuid = ownerId,
                originWorldName = "w",
                originMin = io.papermc.paper.math.Position.block(0, 60, 0),
                originMax = io.papermc.paper.math.Position.block(0, 60, 0),
                orientation = net.azisaba.vanilife.islands.portal.finder.DetectedPortal.Orientation.XY,
                innerWidth = 1,
                innerHeight = 1,
                resourceWorldName = "r",
                resourceMin = io.papermc.paper.math.Position.block(0, 60, 0),
                resourceMax = io.papermc.paper.math.Position.block(0, 60, 0),
                hologramUuid = null,
                createdAt = 0L,
                active = true,
            )

            val manager = PortalManager(mockPlugin, mockRepo, mockIslandRepo, this, mockSpawner)
            coEvery { mockRepo.findActive() } returns listOf(p)

            startKoin { modules(module { single<PortalManager> { manager }; single<PortalRepository> { mockRepo }; single<org.bukkit.plugin.Plugin> { mockPlugin } }) }

            val cmd = PortalCommands()

            // load the portal into manager
            manager.loadAll()
            this.testScheduler.advanceUntilIdle()

            val player = mockk<org.bukkit.entity.Player>(relaxed = true)
            every { player.uniqueId } returns ownerId
            every { player.hasPermission(any<String>()) } returns false
            every { player.isOp } returns false

            val command = mockk<Command>(relaxed = true)

            val result = cmd.onCommand(player, command, "portal", arrayOf("unlink"))
            result shouldBe true

            // allow async removal to run
            this.testScheduler.advanceUntilIdle()

            // verify manager removed the portal and player was notified
            manager.getAllPortals().any { it.id == p.id } shouldBe false
            verify { player.sendMessage(any<net.kyori.adventure.text.Component>()) }
        }
    }

    "giveigniter by player gives igniter to self and sends message" {
        runTest {
            val mockManager = mockk<PortalManager>(relaxed = true)
            val mockRepo = mockk<PortalRepository>(relaxed = true)
            val mockPlugin = mockk<org.bukkit.plugin.Plugin>(relaxed = true)
            every { mockPlugin.name } returns "vanilife"

            startKoin { modules(module { single<PortalManager> { mockManager }; single<PortalRepository> { mockRepo }; single<org.bukkit.plugin.Plugin> { mockPlugin } }) }

            val cmd = PortalCommands()

            val player = mockk<org.bukkit.entity.Player>(relaxed = true)
            val inv = mockk<org.bukkit.inventory.PlayerInventory>(relaxed = true)
            every { player.inventory } returns inv
            every { player.hasPermission("vanilife.portal.giveigniter") } returns true
            every { player.isOp } returns false

            // no-op: relaxed mock inventory will accept addItem calls

            val result = cmd.onCommand(player, mockk(), "portal", arrayOf("giveigniter"))
            result shouldBe true

            verify { inv.addItem(any()) }
            verify { player.sendMessage(any<net.kyori.adventure.text.Component>()) }
        }
    }

    "giveigniter by admin gives igniter to other player and notifies both" {
        runTest {
            val mockManager = mockk<PortalManager>(relaxed = true)
            val mockRepo = mockk<PortalRepository>(relaxed = true)
            val mockPlugin = mockk<org.bukkit.plugin.Plugin>(relaxed = true)
            every { mockPlugin.name } returns "vanilife"

            startKoin { modules(module { single<PortalManager> { mockManager }; single<PortalRepository> { mockRepo }; single<org.bukkit.plugin.Plugin> { mockPlugin } }) }

            val cmd = PortalCommands()

            val sender = mockk<CommandSender>(relaxed = true)
            every { sender.hasPermission("vanilife.portal.giveigniter") } returns true
            every { sender.isOp } returns false

            val target = mockk<org.bukkit.entity.Player>(relaxed = true)
            val inv = mockk<org.bukkit.inventory.PlayerInventory>(relaxed = true)
            every { target.inventory } returns inv
            every { target.name } returns "target"

            every { Bukkit.getPlayer("target") } returns target

            // no-op: relaxed mock inventory will accept addItem calls

            val result = cmd.onCommand(sender, mockk(), "portal", arrayOf("giveigniter", "target"))
            result shouldBe true

            verify { inv.addItem(any()) }
            verify { target.sendMessage(any<net.kyori.adventure.text.Component>()) }
            verify { sender.sendMessage(any<net.kyori.adventure.text.Component>()) }
        }
    }

    "giveigniter without permission should reject and not give item" {
        runTest {
            val mockManager = mockk<PortalManager>(relaxed = true)
            val mockRepo = mockk<PortalRepository>(relaxed = true)
            val mockPlugin = mockk<org.bukkit.plugin.Plugin>(relaxed = true)
            every { mockPlugin.name } returns "vanilife"

            startKoin { modules(module { single<PortalManager> { mockManager }; single<PortalRepository> { mockRepo }; single<org.bukkit.plugin.Plugin> { mockPlugin } }) }

            val cmd = PortalCommands()

            val sender = mockk<CommandSender>(relaxed = true)
            every { sender.hasPermission("vanilife.portal.giveigniter") } returns false
            every { sender.isOp } returns false

            val result = cmd.onCommand(sender, mockk(), "portal", arrayOf("giveigniter"))
            result shouldBe true

            verify { sender.sendMessage(any<net.kyori.adventure.text.Component>()) }
        }
    }

    "unlink by id when admin should remove portal and notify" {
        runTest {
            val mockRepo = mockk<PortalRepository>(relaxed = true)
            val mockPlugin = mockk<org.bukkit.plugin.Plugin>(relaxed = true)
            val mockIslandRepo = mockk<net.azisaba.vanilife.islands.storage.IslandRepository>(relaxed = true)
            val mockSpawner = mockk<HologramSpawner>(relaxed = true)
            every { mockPlugin.name } returns "vanilife"

            val p = Portal(
                id = 99L,
                ownerUuid = java.util.UUID.randomUUID(),
                originWorldName = "w",
                originMin = io.papermc.paper.math.Position.block(0, 60, 0),
                originMax = io.papermc.paper.math.Position.block(0, 60, 0),
                orientation = net.azisaba.vanilife.islands.portal.finder.DetectedPortal.Orientation.XY,
                innerWidth = 1,
                innerHeight = 1,
                resourceWorldName = "r",
                resourceMin = io.papermc.paper.math.Position.block(0, 60, 0),
                resourceMax = io.papermc.paper.math.Position.block(0, 60, 0),
                hologramUuid = null,
                createdAt = 0L,
                active = true,
            )

            coEvery { mockRepo.findActive() } returns listOf(p)

            val manager = PortalManager(mockPlugin, mockRepo, mockIslandRepo, this, mockSpawner)

            startKoin { modules(module { single<PortalManager> { manager }; single<PortalRepository> { mockRepo }; single<org.bukkit.plugin.Plugin> { mockPlugin } }) }

            // load portals into manager
            manager.loadAll()
            this.testScheduler.advanceUntilIdle()

            val cmd = PortalCommands()

            val sender = mockk<CommandSender>(relaxed = true)
            every { sender.hasPermission("vanilife.portal.unlink") } returns true
            every { sender.isOp } returns false

            val result = cmd.onCommand(sender, mockk(), "portal", arrayOf("unlink", "99"))
            result shouldBe true

            // allow async removal to complete
            this.testScheduler.advanceUntilIdle()

            // verify manager removed portal and sender notified
            manager.getAllPortals().any { it.id == p.id } shouldBe false
            verify { sender.sendMessage(any<net.kyori.adventure.text.Component>()) }
        }
    }
})
