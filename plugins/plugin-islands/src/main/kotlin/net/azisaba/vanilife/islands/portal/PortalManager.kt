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

    fun createPortal(ownerUuid: java.util.UUID, origin: net.azisaba.vanilife.islands.portal.finder.DetectedPortal) {
        // choose random resource world location (stubbed)
        val resourceWorld = Bukkit.getWorlds().firstOrNull { it.name == "resources" } ?: Bukkit.getWorlds().first()
        val centerX = resourceWorld.spawnLocation.blockX
        val centerY = resourceWorld.spawnLocation.blockY
        val centerZ = resourceWorld.spawnLocation.blockZ

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
            resourceCenterX = centerX,
            resourceCenterY = centerY,
            resourceCenterZ = centerZ,
            createdAt = System.currentTimeMillis(),
            active = true,
        )

        CoroutineScope(Dispatchers.IO).launch {
            val stored = repository.insert(portal)
            stored.id?.let { portalsById[it] = stored }
        }
    }
}
