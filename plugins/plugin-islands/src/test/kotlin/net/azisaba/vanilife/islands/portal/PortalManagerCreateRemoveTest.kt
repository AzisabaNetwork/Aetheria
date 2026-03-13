package net.azisaba.vanilife.islands.portal

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.bukkit.Bukkit
import io.papermc.paper.math.Position

class PortalManagerCreateRemoveTest : StringSpec({
    afterTest {
        clearAllMocks()
        try { unmockkStatic(Bukkit::class) } catch (_: Exception) {}
        try { unmockkObject(ResourcePortals) } catch (_: Exception) {}
    }

    "createPortal persists portal and updates hologram uuid when spawner returns uuid" {
        runTest {
            val mockRepo = mockk<PortalRepository>(relaxed = true)
            val mockIslandRepo = mockk<net.azisaba.vanilife.islands.storage.IslandRepository>(relaxed = true)
            val mockPlugin = mockk<org.bukkit.plugin.Plugin>(relaxed = true)

            // resource world used for spawn fallback
            val resourceWorld = mockk<org.bukkit.World>(relaxed = true)
            every { resourceWorld.spawnLocation } returns org.bukkit.Location(resourceWorld, 100.0, 60.0, 100.0)
            mockkStatic(Bukkit::class)
            every { Bukkit.getWorld("resources") } returns resourceWorld

            // repository.insert should return stored portal with id assigned
            coEvery { mockRepo.insert(any()) } answers {
                val arg = it.invocation.args[0] as Portal
                arg.copy(id = 10L)
            }

            // hologram spawner returns a UUID
            val expectedUuid = java.util.UUID.randomUUID()
            val spawner = mockk<HologramSpawner>()
            coEvery { spawner.spawnHologram(any(), any()) } returns expectedUuid

            // avoid animation side-effects
            mockkObject(ResourcePortals)
            every { ResourcePortals.createWithAnimation(any(), any()) } returns Unit

            val manager = PortalManager(mockPlugin, mockRepo, mockIslandRepo, this, spawner)

            val originWorld = mockk<org.bukkit.World>(relaxed = true)
            every { originWorld.name } returns "origin"
            val origin = net.azisaba.vanilife.islands.portal.finder.DetectedPortal(
                originWorld,
                1,
                1,
                Position.block(0, 60, 0),
                Position.block(0, 60, 0),
                net.azisaba.vanilife.islands.portal.finder.DetectedPortal.Orientation.XY
            )

            val cfg = net.azisaba.vanilife.islands.PortalConfig(resourceWorld = "resources", spawnAttempts = 0)

            manager.createPortal(java.util.UUID.randomUUID(), origin, cfg)

            // allow async operations to complete
            this.testScheduler.advanceUntilIdle()

            coVerify(exactly = 1) { mockRepo.insert(any()) }

            val stored = manager.getAllPortals().find { it.id == 10L }
            stored.shouldNotBe(null)
            stored!!.hologramUuid shouldBe expectedUuid

            unmockkObject(ResourcePortals)
        }
    }

    "removePortal deletes portal and removes it from in-memory map" {
        runTest {
            val mockRepo = mockk<PortalRepository>(relaxed = true)
            val mockIslandRepo = mockk<net.azisaba.vanilife.islands.storage.IslandRepository>(relaxed = true)
            val mockPlugin = mockk<org.bukkit.plugin.Plugin>(relaxed = true)
            val mockSpawner = mockk<HologramSpawner>(relaxed = true)

            val p = Portal(
                id = 42L,
                ownerUuid = java.util.UUID.randomUUID(),
                originWorldName = "w",
                originMin = Position.block(0, 60, 0),
                originMax = Position.block(0, 60, 0),
                orientation = net.azisaba.vanilife.islands.portal.finder.DetectedPortal.Orientation.XY,
                innerWidth = 1,
                innerHeight = 1,
                resourceWorldName = "r",
                resourceMin = Position.block(0, 60, 0),
                resourceMax = Position.block(0, 60, 0),
                hologramUuid = null,
                createdAt = 0L,
                active = true
            )

            coEvery { mockRepo.findActive() } returns listOf(p)

            val manager = PortalManager(mockPlugin, mockRepo, mockIslandRepo, this, mockSpawner)

            manager.loadAll()
            this.testScheduler.advanceUntilIdle()

            manager.getPortalById(42L)!!.id shouldBe 42L

            coEvery { mockRepo.delete(42L) } returns Unit

            // make sure Bukkit.getWorld("r") returns null so block removal is skipped
            mockkStatic(Bukkit::class)
            every { Bukkit.getWorld("r") } returns null

            manager.removePortal(p)
            this.testScheduler.advanceUntilIdle()

            coVerify(exactly = 1) { mockRepo.delete(42L) }
            manager.getPortalById(42L) shouldBe null
        }
    }

})
