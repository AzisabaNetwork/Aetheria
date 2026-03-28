package net.azisaba.vanilife.island

import org.bukkit.OfflinePlayer
import org.koin.core.context.GlobalContext

suspend fun OfflinePlayer.getIsland(): Island? {
    val manager = GlobalContext.get().get<IslandManager>()
    return manager.lookupByOwner(uniqueId)
}
