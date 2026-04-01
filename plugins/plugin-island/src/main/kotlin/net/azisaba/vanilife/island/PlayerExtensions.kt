package net.azisaba.vanilife.island

import kotlinx.coroutines.future.await
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.koin.core.context.GlobalContext

val Player.currentIsland: Island?
    get() = IslandPlayerMap.lookup(uniqueId)

suspend fun Player.teleport(island: Island) {
    teleportAsync(island.spawnPoint).await()
    IslandPlayerMap.put(this, island)
}

suspend fun OfflinePlayer.ownedIsland(): Island? {
    val cacheMap = GlobalContext.get().get<IslandCacheMap>()
    return cacheMap.lookup(uniqueId)
}
