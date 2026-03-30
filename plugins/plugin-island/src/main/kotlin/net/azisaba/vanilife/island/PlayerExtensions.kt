package net.azisaba.vanilife.island

import net.azisaba.vanilife.Vanilife
import net.azisaba.vanilife.world.IslandPosition
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Entity
import org.koin.core.context.GlobalContext

val Entity.islandPosition: IslandPosition?
    get() = if (world != Vanilife.getIslandsWorld()) null else IslandPosition.fromBlockPosition(
        location.blockX(), location.blockZ(),
    )

suspend fun OfflinePlayer.ownedIsland(): Island? {
    val islandCacheMap = GlobalContext.get().get<IslandCacheMap>()
    return islandCacheMap.lookup(uniqueId)
}
