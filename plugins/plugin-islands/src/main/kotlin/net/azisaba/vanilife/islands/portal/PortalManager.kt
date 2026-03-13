package net.azisaba.vanilife.islands.portal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.regionDispatcher
import net.azisaba.vanilife.islands.PortalConfig
import net.azisaba.vanilife.islands.IslandsFonts
import net.azisaba.vanilife.islands.portal.finder.DetectedPortal
import net.azisaba.vanilife.islands.storage.IslandRepository
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.plugin.Plugin
import java.util.concurrent.ConcurrentHashMap

internal class PortalManager(private val plugin: Plugin, private val repository: PortalRepository, private val islandRepository: net.azisaba.vanilife.islands.storage.IslandRepository) {
    private val portalsById: MutableMap<Long, Portal> = ConcurrentHashMap()
    // worldName -> chunkKey -> set of portal ids (origin-side index for quick lookup on block breaks)
    private val originChunkIndex: MutableMap<String, MutableMap<Long, MutableSet<Long>>> = ConcurrentHashMap()

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
            active.forEach { p ->
                p.id?.let { id ->
                    portalsById[id] = p
                    indexPortalOrigin(p)
                }
            }
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
            if (!block.type.isSolid) return@repeat

            // enforce minPortalDistance: ensure candidate is not too close to existing portals
            if (config.minPortalDistance > 0) {
                val minDistSq = config.minPortalDistance.toDouble() * config.minPortalDistance.toDouble()
                val existing = getPortalsInWorld(world.name)
                var tooClose = false
                for (op in existing) {
                    val ox = (op.resourceMin.blockX() + op.resourceMax.blockX() + 1) / 2.0
                    val oz = (op.resourceMin.blockZ() + op.resourceMax.blockZ() + 1) / 2.0
                    val dx2 = ox - x.toDouble()
                    val dz2 = oz - z.toDouble()
                    if (dx2 * dx2 + dz2 * dz2 < minDistSq) { tooClose = true; break }
                }
                if (tooClose) return@repeat
            }

            chosenTriple = Triple(x, y, z)
            return@repeat
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
            stored.id?.let { id ->
                portalsById[id] = stored
                indexPortalOrigin(stored)
            }

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

                    // spawn hologram using EntityLib wrappers for consistent visuals and font
                    plugin.launch(plugin.regionDispatcher(loc)) {
                        try {
                            // Use TextDisplay entity but configure via Adventure component for text and use font key from IslandsFonts
                            // Prefer EntityLib wrapper PortalHologram for consistent visuals; fallback to TextDisplay spawn
                            val textComp = Component.text("Resource Portal").font(IslandsFonts.WAVES.key())
                            try {
                                // spawn via EntityLib wrapper
                                val container = me.tofaa.entitylib.container.EntityContainer.basic()
                                val wrapper = PortalHologram(textComp)
                                val peLoc = com.github.retrooper.packetevents.protocol.world.Location(centerX, centerY, centerZ, 0f, 0f)
                                val spawned = wrapper.spawn(peLoc, container)
                                if (spawned) {
                                    // find the spawned entity UUID from world entities near center
                                    val world = resourceDetected.world
                                    val found = world.entities.find { it.location.distance(org.bukkit.Location(world, centerX, centerY, centerZ)) < 2.0 }
                                    val uuid = found?.uniqueId
                                    try {
                                        repository.updateHologram(id, uuid)
                                        val current = portalsById[id]
                                        if (current != null) portalsById[id] = current.copy(hologramUuid = uuid)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                } else {
                                    // fallback to TextDisplay
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
                                        val current = portalsById[id]
                                        if (current != null) portalsById[id] = current.copy(hologramUuid = textDisplay.uniqueId)
                                    } catch (e: Exception) { e.printStackTrace() }
                                }
                            } catch (e: Throwable) {
                                // fallback
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
                                    val current = portalsById[id]
                                    if (current != null) portalsById[id] = current.copy(hologramUuid = textDisplay.uniqueId)
                                } catch (ex: Exception) {
                                    ex.printStackTrace()
                                }
                            }
                        } catch (e: Throwable) {
                            // fallback: if anything fails, still continue without hologram
                            e.printStackTrace()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun chunkKey(cx: Int, cz: Int): Long = (cx.toLong() shl 32) or (cz.toLong() and 0xffffffffL)

    private fun indexPortalOrigin(portal: Portal) {
        val id = portal.id ?: return
        val world = portal.originWorldName
        val map = originChunkIndex.computeIfAbsent(world) { ConcurrentHashMap() }
        val minChunkX = portal.originMin.blockX() shr 4
        val maxChunkX = portal.originMax.blockX() shr 4
        val minChunkZ = portal.originMin.blockZ() shr 4
        val maxChunkZ = portal.originMax.blockZ() shr 4
        for (cx in minChunkX..maxChunkX) {
            for (cz in minChunkZ..maxChunkZ) {
                val key = chunkKey(cx, cz)
                val set = map.computeIfAbsent(key) { java.util.concurrent.ConcurrentHashMap.newKeySet<Long>() }
                set.add(id)
            }
        }
    }

    fun unindexPortalOrigin(portal: Portal) {
        val id = portal.id ?: return
        val world = portal.originWorldName
        val map = originChunkIndex[world] ?: return
        val minChunkX = portal.originMin.blockX() shr 4
        val maxChunkX = portal.originMax.blockX() shr 4
        val minChunkZ = portal.originMin.blockZ() shr 4
        val maxChunkZ = portal.originMax.blockZ() shr 4
        for (cx in minChunkX..maxChunkX) {
            for (cz in minChunkZ..maxChunkZ) {
                val key = chunkKey(cx, cz)
                val set = map[key]
                set?.remove(id)
                if (set == null || set.isEmpty()) map.remove(key)
            }
        }
        if (map.isEmpty()) originChunkIndex.remove(world)
    }

    fun getPortalById(id: Long): Portal? = portalsById[id]

    fun getPortalIdsForOriginChunk(worldName: String, chunkX: Int, chunkZ: Int): Set<Long> {
        val map = originChunkIndex[worldName] ?: return emptySet()
        val set = map[chunkKey(chunkX, chunkZ)] ?: return emptySet()
        return set.toSet()
    }

    /**
     * Rebuild the entire origin chunk index from the in-memory portal map.
     * Useful for admin reindex operations if the index becomes stale.
     */
    fun rebuildOriginIndex() {
        originChunkIndex.clear()
        portalsById.values.forEach { p -> indexPortalOrigin(p) }
    }
}
