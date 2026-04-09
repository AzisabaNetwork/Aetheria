package net.azisaba.vanilife.island.lookup

import net.azisaba.vanilife.island.IslandType
import net.azisaba.vanilife.island.findPosition
import net.azisaba.vanilife.world.IslandPosition
import org.jetbrains.exposed.v1.jdbc.Database
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

internal object OwnerLookup {
    private val positionByOwner: ConcurrentMap<UUID, IslandPosition> = ConcurrentHashMap()

    operator fun get(ownerUuid: UUID): IslandPosition? = positionByOwner[ownerUuid]

    suspend fun getOrFind(ownerUuid: UUID, database: Database): IslandPosition? =
        get(ownerUuid) ?: IslandType.PLAYER.findPosition(ownerUuid, database).also { positionByOwner[ownerUuid] = it }

    fun bind(ownerUuid: UUID, position: IslandPosition) {
        val previous = positionByOwner.putIfAbsent(ownerUuid, position)
        check(previous == null || previous == position) {
            "Owner $ownerUuid is already bound to $previous"
        }
    }
}
