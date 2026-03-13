package net.azisaba.vanilife.islands.portal

import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.regionDispatcher
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

internal class DefaultHologramSpawner(private val plugin: Plugin, private val repository: net.azisaba.vanilife.islands.portal.PortalRepository) : HologramSpawner {
    override suspend fun spawnHologram(stored: Portal, resourceDetected: net.azisaba.vanilife.islands.portal.finder.DetectedPortal): java.util.UUID? {
        var resultUuid: java.util.UUID? = null
        val id = stored.id ?: return null
        try {
            val centerX = (resourceDetected.minBound.blockX() + resourceDetected.maxBound.blockX() + 1) / 2.0
            val centerY = (resourceDetected.minBound.blockY() + resourceDetected.maxBound.blockY() + 1) / 2.0
            val centerZ = (resourceDetected.minBound.blockZ() + resourceDetected.maxBound.blockZ() + 1) / 2.0
            val loc = org.bukkit.Location(resourceDetected.world, centerX, centerY, centerZ)

            // run hologram spawn on region dispatcher for Folia safety
            plugin.launch(plugin.regionDispatcher(loc)) {
                try {
                    val textComp = Component.text("Resource Portal").font(IslandsFonts.WAVES.key())
                    try {
                        // Try EntityLib wrapper first
                        val container = me.tofaa.entitylib.container.EntityContainer.basic()
                        val wrapper = PortalHologram(textComp)
                        val peLoc = com.github.retrooper.packetevents.protocol.world.Location(centerX, centerY, centerZ, 0f, 0f)
                        val spawned = wrapper.spawn(peLoc, container)
                        if (spawned) {
                            val world = resourceDetected.world
                            val found = world.entities.find { it.location.distance(org.bukkit.Location(world, centerX, centerY, centerZ)) < 2.0 }
                            val uuid = found?.uniqueId
                            try {
                                repository.updateHologram(id, uuid)
                                resultUuid = uuid
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            // fallback TextDisplay
                            val textDisplay = resourceDetected.world.spawn(loc, org.bukkit.entity.TextDisplay::class.java) {
                                it.isPersistent = false
                                it.text(textComp)
                                it.setLineWidth(40)
                                it.setBackgroundColor(Color.fromRGB(0, 0, 0))
                                it.setTextOpacity(255.toByte())
                                it.setShadowed(true)
                                it.setAlignment(org.bukkit.entity.TextDisplay.TextAlignment.CENTER)
                            }
                            try {
                                repository.updateHologram(id, textDisplay.uniqueId)
                                resultUuid = textDisplay.uniqueId
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } catch (e: Throwable) {
                        // fallback to TextDisplay
                        try {
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
                            resultUuid = textDisplay.uniqueId
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return resultUuid
    }
}
