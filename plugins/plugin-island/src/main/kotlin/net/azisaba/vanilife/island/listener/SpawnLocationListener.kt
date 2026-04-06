package net.azisaba.vanilife.island.listener

import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent
import kotlinx.coroutines.runBlocking
import net.azisaba.vanilife.island.IslandsAccessor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

internal class SpawnLocationListener(private val islands: IslandsAccessor) : Listener {
    @EventHandler
    fun onAsyncPlayerSpawnLocation(event: AsyncPlayerSpawnLocationEvent) {
        val playerUuid = event.connection.profile.id ?: return
        runBlocking {
            islands.createIfNotExists(playerUuid)
            islands.load(playerUuid)
            val island = islands.byOwner(playerUuid)!!
            event.spawnLocation = island.spawnPoint
        }
    }
}
