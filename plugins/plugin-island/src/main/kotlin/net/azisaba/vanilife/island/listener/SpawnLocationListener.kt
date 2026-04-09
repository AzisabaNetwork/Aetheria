package net.azisaba.vanilife.island.listener

import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent
import kotlinx.coroutines.runBlocking
import net.azisaba.vanilife.island.IslandSource
import net.azisaba.vanilife.island.getPlayerIsland
import net.azisaba.vanilife.island.loadOrInitializePlayerIsland
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.jetbrains.exposed.v1.jdbc.Database

internal class SpawnLocationListener(
    private val islandSource: IslandSource, private val database: Database,
) : Listener {
    @EventHandler
    fun onAsyncPlayerSpawnLocation(event: AsyncPlayerSpawnLocationEvent) {
        val playerUuid = event.connection.profile.id ?: return
        val playerIsland = islandSource.getPlayerIsland(playerUuid) ?: runBlocking {
            islandSource.loadOrInitializePlayerIsland(playerUuid, database)
        }

        event.spawnLocation = playerIsland.defaultSpawnPoint
    }
}
