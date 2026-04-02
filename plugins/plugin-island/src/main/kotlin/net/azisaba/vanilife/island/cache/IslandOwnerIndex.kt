package net.azisaba.vanilife.island.cache

import net.azisaba.vanilife.world.IslandPosition
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

internal class IslandOwnerIndex {
    private val positionByOwner: ConcurrentMap<UUID, IslandPosition> = ConcurrentHashMap()
    private val ownerByPosition: ConcurrentMap<IslandPosition, UUID> = ConcurrentHashMap()

    fun lookupPosition(owner: UUID): IslandPosition? = positionByOwner[owner]

    fun lookupOwner(position: IslandPosition): UUID? = ownerByPosition[position]

    fun cache(owner: UUID, position: IslandPosition) {
        val previousPosition = positionByOwner.putIfAbsent(owner, position)
        if (previousPosition != null && previousPosition != position) {
            throw IllegalStateException(
                "Owner mapping conflict detected: owner=$owner existingPosition=$previousPosition newPosition=$position"
            )
        }

        val previousOwner = ownerByPosition.putIfAbsent(position, owner)
        if (previousOwner != null && previousOwner != owner) {
            throw IllegalStateException(
                "Position mapping conflict detected: position=$position existingOwner=$previousOwner newOwner=$owner"
            )
        }
    }
}
