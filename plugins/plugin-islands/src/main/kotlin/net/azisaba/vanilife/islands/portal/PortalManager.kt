package net.azisaba.vanilife.islands.portal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.azisaba.vanilife.islands.storage.IslandRepository
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.concurrent.ConcurrentHashMap

class PortalManager(private val plugin: Plugin, private val repository: PortalRepository, private val islandRepository: IslandRepository) {
    private val portalsById: MutableMap<Long, Portal> = ConcurrentHashMap()

    fun loadAll() {
        CoroutineScope(Dispatchers.IO).launch {
            val active = repository.findActive()
            active.forEach { portalsById[it.id!!] = it }
        }
    }

    fun createPortal(ownerUuid: java.util.UUID, origin: net.azisaba.vanilife.islands.portal.finder.DetectedPortal, config: net.azisaba.vanilife.islands.Config.PortalConfig) {
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
            resourceCenterX = cx,
            resourceCenterY = cy,
            resourceCenterZ = cz,
            createdAt = System.currentTimeMillis(),
            active = true,
        )

        CoroutineScope(Dispatchers.IO).launch {
            val stored = repository.insert(portal)
            stored.id?.let { portalsById[it] = stored }
        }
    }
}
