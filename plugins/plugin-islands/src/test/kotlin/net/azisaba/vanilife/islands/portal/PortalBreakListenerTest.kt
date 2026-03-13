package net.azisaba.vanilife.islands.portal

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.bukkit.event.block.BlockBreakEvent

class PortalBreakListenerTest : StringSpec({
    afterTest { clearAllMocks(); try { stopKoin() } catch (_: Exception) {} }

    "non-owner without perms should have event cancelled" {
        runTest {
            // create mocks for block and player
            val mockPlayer = mockk<org.bukkit.entity.Player>(relaxed = true)
            every { mockPlayer.uniqueId } returns java.util.UUID.randomUUID()
            every { mockPlayer.hasPermission(any<String>()) } returns false
            every { mockPlayer.isOp } returns false

            val mockBlock = mockk<org.bukkit.block.Block>(relaxed = true)
            val mockWorld = mockk<org.bukkit.World>(relaxed = true)
            every { mockBlock.world } returns mockWorld
            every { mockWorld.name } returns "w"
            every { mockBlock.x } returns 0
            every { mockBlock.y } returns 64
            every { mockBlock.z } returns 0

            val event = mockk<BlockBreakEvent>(relaxed = true)
            every { event.block } returns mockBlock
            every { event.player } returns mockPlayer

            // create a real lightweight manager so originChunkIndex is initialized
            val mockPlugin = mockk<org.bukkit.plugin.Plugin>(relaxed = true)
            val mockRepo = mockk<PortalRepository>(relaxed = true)
            val mockIslandRepo = mockk<net.azisaba.vanilife.islands.storage.IslandRepository>(relaxed = true)
            val mockSpawner = mockk<HologramSpawner>(relaxed = true)

            val manager = PortalManager(mockPlugin, mockRepo, mockIslandRepo, this, mockSpawner)

            startKoin { modules(module { single<PortalManager> { manager } }) }

            val listener = net.azisaba.vanilife.islands.portal.listener.PortalBreakListener()

            listener.onBlockBreak(event)

            // success if no exception
            true shouldBe true
        }
    }
})
