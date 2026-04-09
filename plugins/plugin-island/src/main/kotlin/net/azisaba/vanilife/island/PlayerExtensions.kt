package net.azisaba.vanilife.island

import kotlinx.coroutines.future.await
import net.azisaba.vanilife.island.lookup.OwnerLookup
import net.azisaba.vanilife.island.lookup.PlayerLookup
import org.bukkit.entity.Player
import org.koin.core.context.GlobalContext

val Player.currentIsland: Island?
    get() {
        val islandSource = GlobalContext.get().get<IslandSource>()
        val position = PlayerLookup[uniqueId]
        return position?.let(islandSource::get)
    }

val Player.ownedIsland: PlayerIsland
    get() {
        val islandSource = GlobalContext.get().get<IslandSource>()
        val position = OwnerLookup[uniqueId] ?: error("Not bound to owner lookup: $uniqueId")
        return islandSource[position] as? PlayerIsland ?: error("Player island not loaded: $position")
    }

val Player.isInOwnedIsland: Boolean
    get() = currentIsland == ownedIsland

suspend fun Player.teleport(island: Island) {
    currentIsland?.removePlayer(this)
    teleportAsync(island.defaultSpawnPoint).await()
    island.addPlayer(this)
}
