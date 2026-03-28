package net.azisaba.vanilife.island

import org.bukkit.OfflinePlayer
import org.koin.core.context.GlobalContext

suspend fun OfflinePlayer.getIsland(): Island? {
    val islandMap = GlobalContext.get().get<IslandMap>()
    return islandMap.lookup(uniqueId)
}
