package net.azisaba.vanilife.island

import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

internal object IslandsPlayerAccessor {
    private val players: ConcurrentMap<UUID, Island> = ConcurrentHashMap()

    fun byPlayer(uuid: UUID): Island? = players[uuid]

    fun byIsland(island: Island): Set<UUID> = players.filterValues { it == island }.keys

    suspend fun assign(player: Player, island: Island) {
        val previousIsland = byPlayer(player.uniqueId)
        if (previousIsland === island) return

        previousIsland?.removePlayer(player)
        island.addPlayer(player)

        players[player.uniqueId] = island
    }

    suspend fun unassign(player: Player) {
        val island = byPlayer(player.uniqueId) ?: return
        island.removePlayer(player)
        players.remove(player.uniqueId)
    }
}
