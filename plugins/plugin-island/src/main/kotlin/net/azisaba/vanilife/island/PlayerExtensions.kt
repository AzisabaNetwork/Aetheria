package net.azisaba.vanilife.island

import kotlinx.coroutines.future.await
import net.azisaba.vanilife.island.cache.IslandCacheMap
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.koin.core.context.GlobalContext

val Player.currentIsland: Island?
    get() = IslandPlayerMap.lookup(uniqueId)

suspend fun Player.teleport(island: Island) {
    teleportAsync(island.spawnPoint).await()
    IslandPlayerMap.put(this, island)
}

suspend fun Player.ownedIsland(): Island = (this as OfflinePlayer).ownedIsland()!!

suspend fun OfflinePlayer.ownedIsland(): Island? {
    val cacheMap = GlobalContext.get().get<IslandCacheMap>()
    return cacheMap.lookup(uniqueId)
}
