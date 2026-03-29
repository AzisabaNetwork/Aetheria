package net.azisaba.vanilife.island

import net.azisaba.vanilife.world.IslandPosition
import net.azisaba.vanilife.world.IslandsWorld
import org.koin.core.context.GlobalContext

suspend fun IslandsWorld.getIslandAt(pos: IslandPosition): Island? {
    val islandMap = GlobalContext.get().get<IslandMap>()
    return islandMap.lookup(pos)
}
