package net.azisaba.vanilife.island.lookup

import net.azisaba.vanilife.world.IslandPosition
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

internal object PlayerLookup {
    private val positionByPlayer: ConcurrentMap<UUID, IslandPosition> = ConcurrentHashMap()

    operator fun get(playerUuid: UUID): IslandPosition? = positionByPlayer[playerUuid]

    operator fun get(player: Player): IslandPosition? = get(player.uniqueId)

    fun bind(player: Player, position: IslandPosition) {
        val playerUuid = player.uniqueId
        val previous = positionByPlayer.putIfAbsent(playerUuid, position)
        check(previous == null || previous == position) {
            "Player $playerUuid is already bound to $previous"
        }
    }

    fun unbind(player: Player) {
        val playerUuid = player.uniqueId
        val removed = positionByPlayer.remove(playerUuid)
        check(removed != null) {
            "Player $playerUuid is not bound"
        }
    }
}
