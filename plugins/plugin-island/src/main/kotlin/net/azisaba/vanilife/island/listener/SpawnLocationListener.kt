package net.azisaba.vanilife.island.listener

import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent
import kotlinx.coroutines.runBlocking
import net.azisaba.vanilife.island.IslandCacheMap
import net.azisaba.vanilife.island.IslandSpawnPointFinder
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

internal class SpawnLocationListener(private val cacheMap: IslandCacheMap) : Listener {
    @EventHandler
    fun onAsyncPlayerSpawnLocation(event: AsyncPlayerSpawnLocationEvent) {
        val playerUuid = event.connection.profile.id ?: return
        runBlocking {
            val island = cacheMap.lookupOrCreate(playerUuid)
            val spawnPoint = island.spawnPoint()
            event.spawnLocation = if (IslandSpawnPointFinder.isSafe(spawnPoint)) {
                spawnPoint
            } else {
                IslandSpawnPointFinder.find(island)?.let { safeSpawnPoint ->
                    island.spawnPoint(safeSpawnPoint)
                    safeSpawnPoint
                } ?: spawnPoint
            }
        }
    }
}
