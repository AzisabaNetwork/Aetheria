package net.azisaba.vanilife.island

import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

object IslandPlayerMap {
    private val map: ConcurrentMap<UUID, Island> = ConcurrentHashMap()

    fun lookup(uuid: UUID): Island? = map[uuid]

    fun collect(island: Island): Set<UUID> = map.filterValues { it == island }.keys

    suspend fun put(player: Player, island: Island) {
        val previousIsland = lookup(player.uniqueId)
        if (previousIsland === island) return

        previousIsland?.removePlayer(player)
        island.addPlayer(player)

        map[player.uniqueId] = island
    }

    suspend fun remove(player: Player) {
        val island = lookup(player.uniqueId) ?: return
        island.removePlayer(player)
        map.remove(player.uniqueId)
    }
}
