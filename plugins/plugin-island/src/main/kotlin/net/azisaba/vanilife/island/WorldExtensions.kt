package net.azisaba.vanilife.island

import io.papermc.paper.math.Position
import net.azisaba.vanilife.world.IslandPosition
import net.azisaba.vanilife.world.IslandsWorld
import org.koin.core.context.GlobalContext

val IslandsWorld.loadedIslands: Collection<Island>
    get() = GlobalContext.get().get<IslandSource>().loadedIslands

fun IslandsWorld.getIslandAt(position: IslandPosition): Island? {
    val islandSource = GlobalContext.get().get<IslandSource>()
    return islandSource[position]
}

fun IslandsWorld.getIslandAt(position: Position): Island? =
    getIslandAt(IslandPosition.fromPosition(position))
