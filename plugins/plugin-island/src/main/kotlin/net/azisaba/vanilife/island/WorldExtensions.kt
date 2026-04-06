package net.azisaba.vanilife.island

import io.papermc.paper.math.Position
import net.azisaba.vanilife.world.IslandPosition
import net.azisaba.vanilife.world.IslandsWorld
import org.koin.core.context.GlobalContext

val IslandsWorld.loadedIslands: Collection<Island>
    get() {
        val islands = GlobalContext.get().get<IslandsAccessor>()
        return islands.islands
    }

fun IslandsWorld.getIslandAt(position: IslandPosition): Island? {
    val islands = GlobalContext.get().get<IslandsAccessor>()
    return islands.byPosition(position)
}

fun IslandsWorld.getIslandAt(position: Position): Island? = getIslandAt(IslandPosition.fromPosition(position))
