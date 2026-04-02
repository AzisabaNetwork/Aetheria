package net.azisaba.vanilife.island.cache

import net.azisaba.vanilife.island.Island
import net.azisaba.vanilife.world.IslandPosition
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

internal class IslandInstanceRegistry {
    private val islandByPosition: ConcurrentMap<IslandPosition, Island> = ConcurrentHashMap()

    fun lookup(position: IslandPosition): Island? = islandByPosition[position]

    fun cache(position: IslandPosition, island: Island) {
        islandByPosition[position] = island
    }

    fun iterator(): Iterator<Island> = islandByPosition.values.iterator()
}
