package net.azisaba.vanilife.island

import net.azisaba.vanilife.world.IslandPosition
import net.azisaba.vanilife.world.IslandsWorld
import org.koin.core.context.GlobalContext

suspend fun IslandsWorld.getIslandAt(pos: IslandPosition): Island? {
    val islandCacheMap = GlobalContext.get().get<IslandCacheMap>()
    return islandCacheMap.lookup(pos)
}
