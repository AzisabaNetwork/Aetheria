package net.azisaba.vanilife.islands.portal

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.nulls.shouldBeNull
import io.mockk.clearAllMocks
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import io.papermc.paper.math.Position

class HologramSpawnerTest : StringSpec({
    afterTest { clearAllMocks() }

    "spawnHologram returns null when portal id is null" {
        runTest {
            val mockRepo = mockk<PortalRepository>(relaxed = true)
            val mockPlugin = mockk<org.bukkit.plugin.Plugin>(relaxed = true)
            val mockWorld = mockk<org.bukkit.World>(relaxed = true)

            val p = Portal(
                id = null,
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

            val detected = net.azisaba.vanilife.islands.portal.finder.DetectedPortal(
                mockWorld, 1, 1, Position.block(0, 60, 0), Position.block(0, 60, 0), net.azisaba.vanilife.islands.portal.finder.DetectedPortal.Orientation.XY
            )

            val spawner = DefaultHologramSpawner(mockPlugin, mockRepo, dispatcherProvider = { Dispatchers.Unconfined })

            val result = spawner.spawnHologram(p, detected)
            result.shouldBeNull()

            coVerify(exactly = 0) { mockRepo.updateHologram(any(), any()) }
        }
    }

    "custom entityLib spawner result updates repository and is returned" {
        runTest {
            val mockRepo = mockk<PortalRepository>(relaxed = true)
            val mockPlugin = mockk<org.bukkit.plugin.Plugin>(relaxed = true)
            val mockWorld = mockk<org.bukkit.World>(relaxed = true)

            val id = 123L
            val p = Portal(
                id = id,
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

            val detected = net.azisaba.vanilife.islands.portal.finder.DetectedPortal(
                mockWorld, 1, 1, Position.block(0, 60, 0), Position.block(0, 60, 0), net.azisaba.vanilife.islands.portal.finder.DetectedPortal.Orientation.XY
            )

            val expectedUuid = java.util.UUID.randomUUID()

            val entityLibSpawner: EntityLibHologramSpawner = { _id, _component, _rd, _loc, _cx, _cy, _cz -> expectedUuid }

            val spawner = DefaultHologramSpawner(mockPlugin, mockRepo, dispatcherProvider = { Dispatchers.Unconfined }, entityLibSpawner = entityLibSpawner)

            val result = spawner.spawnHologram(p, detected)
            result shouldBe expectedUuid

            coVerify(exactly = 1) { mockRepo.updateHologram(id, expectedUuid) }
        }
    }

    "entityLib returns null and textDisplay custom spawner is used" {
        runTest {
            val mockRepo = mockk<PortalRepository>(relaxed = true)
            val mockPlugin = mockk<org.bukkit.plugin.Plugin>(relaxed = true)
            val mockWorld = mockk<org.bukkit.World>(relaxed = true)

            val id = 321L
            val p = Portal(
                id = id,
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

            val detected = net.azisaba.vanilife.islands.portal.finder.DetectedPortal(
                mockWorld, 1, 1, Position.block(0, 60, 0), Position.block(0, 60, 0), net.azisaba.vanilife.islands.portal.finder.DetectedPortal.Orientation.XY
            )

            val expectedUuid = java.util.UUID.randomUUID()

            val entityLibSpawner: EntityLibHologramSpawner = { _, _, _, _, _, _, _ -> null }
            val textDisplaySpawner: TextDisplayHologramSpawner = { _id, _component, _rd, _loc -> expectedUuid }

            val spawner = DefaultHologramSpawner(mockPlugin, mockRepo, dispatcherProvider = { Dispatchers.Unconfined }, entityLibSpawner = entityLibSpawner, textDisplaySpawner = textDisplaySpawner)

            val result = spawner.spawnHologram(p, detected)
            result shouldBe expectedUuid

            coVerify(exactly = 1) { mockRepo.updateHologram(id, expectedUuid) }
        }
    }

    "both custom spawners return null -> spawnHologram returns null and repository not updated" {
        runTest {
            val mockRepo = mockk<PortalRepository>(relaxed = true)
            val mockPlugin = mockk<org.bukkit.plugin.Plugin>(relaxed = true)
            val mockWorld = mockk<org.bukkit.World>(relaxed = true)

            val id = 555L
            val p = Portal(
                id = id,
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

            val detected = net.azisaba.vanilife.islands.portal.finder.DetectedPortal(
                mockWorld, 1, 1, Position.block(0, 60, 0), Position.block(0, 60, 0), net.azisaba.vanilife.islands.portal.finder.DetectedPortal.Orientation.XY
            )

            val entityLibSpawner: EntityLibHologramSpawner = { _, _, _, _, _, _, _ -> null }
            val textDisplaySpawner: TextDisplayHologramSpawner = { _, _, _, _ -> null }

            val spawner = DefaultHologramSpawner(mockPlugin, mockRepo, dispatcherProvider = { Dispatchers.Unconfined }, entityLibSpawner = entityLibSpawner, textDisplaySpawner = textDisplaySpawner)

            val result = spawner.spawnHologram(p, detected)
            result.shouldBeNull()

            coVerify(exactly = 0) { mockRepo.updateHologram(any(), any()) }
        }
    }

    "dispatcherProvider is invoked with computed center location" {
        runTest {
            val mockRepo = mockk<PortalRepository>(relaxed = true)
            val mockPlugin = mockk<org.bukkit.plugin.Plugin>(relaxed = true)
            val mockWorld = mockk<org.bukkit.World>(relaxed = true)

            val id = 777L
            val p = Portal(
                id = id,
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

            val detected = net.azisaba.vanilife.islands.portal.finder.DetectedPortal(
                mockWorld, 1, 1, Position.block(10, 61, 20), Position.block(12, 65, 22), net.azisaba.vanilife.islands.portal.finder.DetectedPortal.Orientation.XY
            )

            var capturedLoc: org.bukkit.Location? = null

            val entityLibSpawner: EntityLibHologramSpawner = { _id, _component, _rd, _loc, _cx, _cy, _cz -> java.util.UUID.randomUUID() }

            val spawner = DefaultHologramSpawner(
                mockPlugin,
                mockRepo,
                dispatcherProvider = { loc -> capturedLoc = loc; Dispatchers.Unconfined },
                entityLibSpawner = entityLibSpawner
            )

            val result = spawner.spawnHologram(p, detected)
            result shouldNotBe null
            capturedLoc.shouldNotBe(null)
            val loc = capturedLoc!!
            // centerX = (10 + 12 + 1)/2 = 11.5
            loc.x shouldBe 11.5
            // centerY = (61 + 65 +1)/2 = 63.5
            loc.y shouldBe 63.5
            // centerZ = (20 + 22 +1)/2 = 21.5
            loc.z shouldBe 21.5
        }
    }

})
