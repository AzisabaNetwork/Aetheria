package net.azisaba.vanilife.island

import io.papermc.paper.math.Position
import net.azisaba.vanilife.island.cache.IslandCacheMap
import net.azisaba.vanilife.world.IslandPosition
import net.azisaba.vanilife.world.IslandsWorld
import org.koin.core.context.GlobalContext

suspend fun IslandsWorld.getIslandAt(position: IslandPosition): Island? {
    val islandCacheMap = GlobalContext.get().get<IslandCacheMap>()
    return islandCacheMap.lookup(position)
}

suspend fun IslandsWorld.getIslandAt(position: Position): Island? =
    getIslandAt(IslandPosition.fromPosition(position))
