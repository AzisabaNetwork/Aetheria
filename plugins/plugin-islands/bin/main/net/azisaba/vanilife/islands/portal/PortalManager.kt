package net.azisaba.vanilife.islands.portal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.regionDispatcher
import net.azisaba.vanilife.islands.PortalConfig
import net.azisaba.vanilife.islands.portal.finder.DetectedPortal
import net.azisaba.vanilife.islands.storage.IslandRepository
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.concurrent.ConcurrentHashMap

internal class PortalManager(private val plugin: Plugin, private val repository: PortalRepository, private val islandRepository: net.azisaba.vanilife.islands.storage.IslandRepository) {
    private val portalsById: MutableMap<Long, Portal> = ConcurrentHashMap()

    fun getAllPortals(): Collection<Portal> = portalsById.values

    fun removePortal(portal: Portal) {
        CoroutineScope(Dispatchers.IO).launch {
            portal.id?.let {
                repository.delete(it)
                // remove resource blocks and hologram if present
                try {
                    // remove portal blocks
                    val detected = DetectedPortal(
                        Bukkit.getWorld(portal.resourceWorldName)!!,
                        portal.innerWidth,
                        portal.innerHeight,
                        portal.resourceMin,
                        portal.resourceMax,
                        portal.orientation
                    )
                    val minX = detected.minBound.blockX()
                    val maxX = detected.maxBound.blockX()
                    val minY = detected.minBound.blockY()
                    val maxY = detected.maxBound.blockY()
                    val minZ = detected.minBound.blockZ()
                    val maxZ = detected.maxBound.blockZ()

                    val world = Bukkit.getWorld(portal.resourceWorldName) ?: return@launch
                    val centerX = (minX + maxX + 1) / 2.0
                    val centerY = (minY + maxY + 1) / 2.0
                    val centerZ = (minZ + maxZ + 1) / 2.0
                    val loc = org.bukkit.Location(world, centerX, centerY, centerZ)

                    // run block and entity removal on region dispatcher
                    plugin.launch(plugin.regionDispatcher(loc)) {
                        for (x in minX..maxX) {
                            for (y in minY..maxY) {
                                for (z in minZ..maxZ) {
                                    val b = world.getBlockAt(x, y, z)
                                    if (b.type == org.bukkit.Material.NETHER_PORTAL) b.type = org.bukkit.Material.AIR
                                }
                            }
                        }

                        // remove hologram if present
                        portal.hologramUuid?.let { uuid ->
                            val entity = world.entities.find { it.uniqueId == uuid }
                            entity?.remove()
                        }
                    }

                } catch (ex: Exception) {
                    ex.printStackTrace()
                }

                portal.id?.let { portalsById.remove(it) }
            }
        }
    }

    fun getPortalsInWorld(worldName: String): List<Portal> = portalsById.values.filter { it.resourceWorldName == worldName }

    suspend fun lookupIslandForPortal(portal: Portal): net.azisaba.vanilife.islands.IslandInfo? {
        return islandRepository.lookupByOwner(portal.ownerUuid)
    }

    fun loadAll() {
        CoroutineScope(Dispatchers.IO).launch {
            val active = repository.findActive()
            active.forEach { portalsById[it.id!!] = it }
        }
    }

    fun createPortal(ownerUuid: java.util.UUID, origin: DetectedPortal, config: PortalConfig) {
        val resourceWorld = Bukkit.getWorld(config.resourceWorld) ?: Bukkit.getWorlds().first()

        // Random coordinate search (simple implementation): pick random X/Z within radius and use highest block Y
        val world = resourceWorld
        val rand = java.util.Random()
        var chosenTriple: Triple<Int, Int, Int>? = null
        repeat(config.spawnAttempts) {
            val dx = rand.nextInt(config.spawnRadius * 2) - config.spawnRadius
            val dz = rand.nextInt(config.spawnRadius * 2) - config.spawnRadius
            val x = world.spawnLocation.blockX + dx
            val z = world.spawnLocation.blockZ + dz
            val y = world.getHighestBlockYAt(x, z)
            // Basic safety checks: avoid liquid and ensure not inside a portal block
            val block = world.getBlockAt(x, y - 1, z)
            if (block.type.isSolid) {
                chosenTriple = Triple(x, y, z)
                return@repeat
            }
        }

        val (cx, cy, cz) = chosenTriple ?: run {
            // fallback to world spawn
            val sl = resourceWorld.spawnLocation
            Triple(sl.blockX, sl.blockY, sl.blockZ)
        }

        val resourceMin = io.papermc.paper.math.Position.block(cx - 1, cy - 1, cz - 1)
        val resourceMax = io.papermc.paper.math.Position.block(cx + 1, cy + 3, cz + 1)

        val portal = Portal(
            id = null,
            ownerUuid = ownerUuid,
            originWorldName = origin.world.name,
            originMin = origin.minBound,
            originMax = origin.maxBound,
            orientation = origin.orientation,
            innerWidth = origin.innerWidth,
            innerHeight = origin.innerHeight,
            resourceWorldName = resourceWorld.name,
            resourceMin = resourceMin,
            resourceMax = resourceMax,
            hologramUuid = null,
            createdAt = System.currentTimeMillis(),
            active = true,
        )

        // Prepare a DetectedPortal for the resource world so we can create the portal blocks
        val resourceDetected = DetectedPortal(
            resourceWorld,
            origin.innerWidth,
            origin.innerHeight,
            resourceMin,
            resourceMax,
            origin.orientation
        )

        // Persist portal metadata first (IO) and then spawn the resource portal animation
        CoroutineScope(Dispatchers.IO).launch {
            val stored = repository.insert(portal)
            stored.id?.let { portalsById[it] = stored }

            // Create the resource-side portal blocks with animation. ResourcePortals will
            // run the block changes on the region dispatcher internally.
            try {
                ResourcePortals.createWithAnimation(plugin, resourceDetected)
            } catch (ex: Exception) {
                // Log or handle creation failure; for now, print stacktrace so issues are visible during testing
                ex.printStackTrace()
            }

            // Spawn a hologram TextDisplay at the center of the resource portal
            stored.id?.let { id ->
                try {
                    val centerX = (resourceDetected.minBound.blockX() + resourceDetected.maxBound.blockX() + 1) / 2.0
                    val centerY = (resourceDetected.minBound.blockY() + resourceDetected.maxBound.blockY() + 1) / 2.0
                    val centerZ = (resourceDetected.minBound.blockZ() + resourceDetected.maxBound.blockZ() + 1) / 2.0
                    val loc = org.bukkit.Location(resourceDetected.world, centerX, centerY, centerZ)

                    // spawn TextDisplay on region dispatcher
                    plugin.launch(plugin.regionDispatcher(loc)) {
                        val textDisplay = resourceDetected.world.spawn(loc, org.bukkit.entity.TextDisplay::class.java) {
                            it.isPersistent = false
                            it.customName = "§bResource Portal"
                            it.isCustomNameVisible = true
                        }
                        // store hologram UUID in DB
                        try {
                            repository.updateHologram(id, textDisplay.uniqueId)
                            // update in-memory portal entry
                            val current = portalsById[id]
                            if (current != null) {
                                portalsById[id] = current.copy(hologramUuid = textDisplay.uniqueId)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
