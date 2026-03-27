package net.azisaba.vanilife.islands

import net.azisaba.vanilife.islands.repository.PrimaryIslandData
import net.azisaba.vanilife.world.IslandPos
import java.util.*

interface IslandInfo {
    val pos: IslandPos

    val ownerUuid: UUID

    val primaryData: PrimaryIslandData

    val dragonData: DragonMetadata
}

data class IslandSummary(
    override val pos: IslandPos,
    override val ownerUuid: UUID,
    override val primaryData: PrimaryIslandData,
    override val dragonData: DragonMetadata = DragonMetadata.Snapshot()
) : IslandInfo

internal interface IslandInfoLookup {

    suspend fun lookupByPos(islandPos: IslandPos): IslandInfo?
    suspend fun lookupByOwner(ownerUuid: UUID): IslandInfo?
}
