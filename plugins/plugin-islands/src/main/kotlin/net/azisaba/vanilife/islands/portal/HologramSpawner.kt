package net.azisaba.vanilife.islands.portal

import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext
import java.util.logging.Level
import net.azisaba.vanilife.islands.IslandsFonts
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.plugin.Plugin

/**
 * Abstraction for spawning/updating portal holograms. Default implementation uses EntityLib/TextDisplay.
 */
internal interface HologramSpawner {
    suspend fun spawnHologram(stored: Portal, resourceDetected: net.azisaba.vanilife.islands.portal.finder.DetectedPortal): java.util.UUID?
}

internal class DefaultHologramSpawner(
    private val plugin: Plugin,
    private val repository: net.azisaba.vanilife.islands.portal.PortalRepository,
    private val dispatcherProvider: (org.bukkit.Location) -> CoroutineContext = { Dispatchers.Unconfined }
) : HologramSpawner {
    override suspend fun spawnHologram(stored: Portal, resourceDetected: net.azisaba.vanilife.islands.portal.finder.DetectedPortal): java.util.UUID? {
        var resultUuid: java.util.UUID? = null
        val id = stored.id ?: return null

        // compute center location
        val centerX = (resourceDetected.minBound.blockX() + resourceDetected.maxBound.blockX() + 1) / 2.0
        val centerY = (resourceDetected.minBound.blockY() + resourceDetected.maxBound.blockY() + 1) / 2.0
        val centerZ = (resourceDetected.minBound.blockZ() + resourceDetected.maxBound.blockZ() + 1) / 2.0
        val loc = org.bukkit.Location(resourceDetected.world, centerX, centerY, centerZ)

        // run hologram spawn on provided dispatcher (tests may override)
        val dispatcher: CoroutineContext = try { dispatcherProvider(loc) } catch (_: Throwable) { Dispatchers.Unconfined }
        return try {
            withContext(dispatcher) {
                tryEntityLibSpawn(id, resourceDetected, loc, centerX, centerY, centerZ)?.also { uuid ->
                    resultUuid = uuid
                } ?: tryTextDisplaySpawn(id, resourceDetected, loc)?.also { uuid ->
                    resultUuid = uuid
                }
            }
            resultUuid
        } catch (e: Exception) {
            plugin.logger.log(Level.SEVERE, "Hologram spawn failed", e)
            resultUuid
        }
    }

    private fun tryEntityLibSpawn(
        id: Long,
        resourceDetected: net.azisaba.vanilife.islands.portal.finder.DetectedPortal,
        loc: org.bukkit.Location,
        centerX: Double,
        centerY: Double,
        centerZ: Double
    ): java.util.UUID? {
        try {
            val textComp = Component.text("Resource Portal").font(IslandsFonts.WAVES.key())
            val container = me.tofaa.entitylib.container.EntityContainer.basic()
            val wrapper = PortalHologram(textComp)
            val peLoc = com.github.retrooper.packetevents.protocol.world.Location(centerX, centerY, centerZ, 0f, 0f)
            val spawned = wrapper.spawn(peLoc, container)
            if (spawned) {
                val world = resourceDetected.world
                val found = world.entities.find { it.location.distance(org.bukkit.Location(world, centerX, centerY, centerZ)) < 2.0 }
                val uuid = found?.uniqueId
                repository.updateHologram(id, uuid)
                return uuid
            }
        } catch (e: Throwable) {
            // fall through to TextDisplay fallback
            plugin.logger.log(Level.FINE, "EntityLib hologram spawn failed, falling back to TextDisplay", e)
        }
        return null
    }

    private fun tryTextDisplaySpawn(
        id: Long,
        resourceDetected: net.azisaba.vanilife.islands.portal.finder.DetectedPortal,
        loc: org.bukkit.Location
    ): java.util.UUID? {
        try {
            val textComp = Component.text("Resource Portal").font(IslandsFonts.WAVES.key())
            val textDisplay = resourceDetected.world.spawn(loc, org.bukkit.entity.TextDisplay::class.java) {
                it.isPersistent = false
                it.text(textComp)
                it.setLineWidth(40)
                it.setBackgroundColor(Color.fromRGB(0, 0, 0))
                it.setTextOpacity(255.toByte())
                it.setShadowed(true)
                it.setAlignment(org.bukkit.entity.TextDisplay.TextAlignment.CENTER)
            }
            repository.updateHologram(id, textDisplay.uniqueId)
            return textDisplay.uniqueId
        } catch (ex: Exception) {
            plugin.logger.log(Level.SEVERE, "TextDisplay hologram spawn failed", ex)
        }
        return null
    }
}
