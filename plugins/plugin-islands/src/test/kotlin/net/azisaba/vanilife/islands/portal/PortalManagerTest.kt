package net.azisaba.vanilife.islands.portal

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import io.papermc.paper.math.Position

class PortalManagerTest : StringSpec({
    val mockPlugin = mockk<org.bukkit.plugin.Plugin>(relaxed = true)
    val mockRepo = mockk<PortalRepository>(relaxed = true)
    val mockIslandRepo = mockk<net.azisaba.vanilife.islands.storage.IslandRepository>(relaxed = true)
    val mockSpawner = mockk<HologramSpawner>(relaxed = true)

    afterTest {
        clearAllMocks()
    }

    "loadAll should load portals from repository and index their origin chunks" {
        runTest {
            // create a portal whose origin spans block (0..15) in X and Z so it's within chunk 0,0
            val p = Portal(
                id = 42L,
                ownerUuid = java.util.UUID.randomUUID(),
                originWorldName = "world",
                originMin = Position.block(0, 60, 0),
                originMax = Position.block(15, 64, 15),
                orientation = net.azisaba.vanilife.islands.portal.finder.DetectedPortal.Orientation.XY,
                innerWidth = 3,
                innerHeight = 4,
                resourceWorldName = "resource",
                resourceMin = Position.block(100, 60, 100),
                resourceMax = Position.block(102, 64, 102),
                hologramUuid = null,
                createdAt = System.currentTimeMillis(),
                active = true,
            )

            coEvery { mockRepo.findActive() } returns listOf(p)

            val manager = PortalManager(mockPlugin, mockRepo, mockIslandRepo, this, mockSpawner)

            manager.loadAll()
            // allow launched coroutines to run
            // advanceUntilIdle is part of TestScope; use currentTestScheduler
            this.testScheduler.advanceUntilIdle()

            // chunk 0,0 should contain portal id 42
            val ids = manager.getPortalIdsForOriginChunk("world", 0, 0)
            ids shouldContain 42L
            manager.getPortalById(42L)!!.id shouldBe 42L
        }
    }

    "index and unindex should add and remove entries and rebuildOriginIndex should restore index" {
        runTest {
            val p1 = Portal(
                id = 1L,
                ownerUuid = java.util.UUID.randomUUID(),
                originWorldName = "w",
                originMin = Position.block(0, 60, 0),
                originMax = Position.block(0, 61, 0),
                orientation = net.azisaba.vanilife.islands.portal.finder.DetectedPortal.Orientation.XY,
                innerWidth = 1,
                innerHeight = 1,
                resourceWorldName = "r",
                resourceMin = Position.block(0, 60, 0),
                resourceMax = Position.block(0, 60, 0),
                hologramUuid = null,
                createdAt = 0L,
                active = true,
            )

            val manager = PortalManager(mockPlugin, mockRepo, mockIslandRepo, this, mockSpawner)

            // index p1
            // indexPortalOrigin is internal; use reflection to call it for testing
            val indexMethod = manager::class.java.getDeclaredMethod("indexPortalOrigin", net.azisaba.vanilife.islands.portal.Portal::class.java)
            indexMethod.isAccessible = true
            indexMethod.invoke(manager, p1)
            manager.getPortalIdsForOriginChunk("w", 0, 0) shouldContain 1L

            // unindex p1
            val unindexMethod = manager::class.java.getDeclaredMethod("unindexPortalOrigin", net.azisaba.vanilife.islands.portal.Portal::class.java)
            unindexMethod.isAccessible = true
            unindexMethod.invoke(manager, p1)
            manager.getPortalIdsForOriginChunk("w", 0, 0).isEmpty() shouldBe true

            // add into in-memory map and rebuild
            // simulate loaded portals map by directly placing into internal map via public API: add through insert simulation
            // (we cannot access internal map directly) so we'll simulate by calling loadAll with repo returning the portal
            coEvery { mockRepo.findActive() } returns listOf(p1)
            manager.loadAll()
            this.testScheduler.advanceUntilIdle()
            manager.getPortalIdsForOriginChunk("w", 0, 0) shouldContain 1L
        }
    }
})
