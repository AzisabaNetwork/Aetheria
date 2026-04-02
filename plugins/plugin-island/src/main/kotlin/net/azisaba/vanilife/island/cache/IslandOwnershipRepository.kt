package net.azisaba.vanilife.island.cache

import net.azisaba.vanilife.Vanilife
import net.azisaba.vanilife.island.IslandsTable
import net.azisaba.vanilife.world.IslandPosition
import net.azisaba.vanilife.world.IslandsWorld
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.*

internal class IslandOwnershipRepository(private val database: Database) {
    suspend fun lookupOwner(position: IslandPosition): UUID? = suspendTransaction(database) {
        IslandsTable.select(IslandsTable.owner)
            .where { IslandsTable.id eq position.toLong() }
            .singleOrNull()
            ?.get(IslandsTable.owner)
    }

    suspend fun lookupPosition(owner: UUID): IslandPosition? = suspendTransaction(database) {
        IslandsTable.select(IslandsTable.id)
            .where { IslandsTable.owner eq owner }
            .singleOrNull()
            ?.get(IslandsTable.id)
            ?.value
            ?.let(IslandPosition::fromLong)
    }

    suspend fun contains(position: IslandPosition): Boolean = suspendTransaction(database) {
        IslandsTable.select(IslandsTable.id)
            .where { IslandsTable.id eq position.toLong() }
            .singleOrNull() != null
    }

    suspend fun initRecordIfMissing(owner: UUID): IslandRecordInitResult = suspendTransaction(database) {
        val existingPosition = IslandsTable.select(IslandsTable.id)
            .where { IslandsTable.owner eq owner }
            .singleOrNull()
            ?.get(IslandsTable.id)
            ?.value
            ?.let(IslandPosition::fromLong)
        if (existingPosition != null) {
            return@suspendTransaction IslandRecordInitResult(position = existingPosition, initialized = false)
        }

        val position = IslandPosition.fromLong(IslandsTable.insertAndGetId { it[IslandsTable.owner] = owner }.value)
        updateDefaultSpawn(position)
        IslandRecordInitResult(position = position, initialized = true)
    }

    private fun updateDefaultSpawn(position: IslandPosition) {
        val seed = Vanilife.getIslandsWorld().seed
        val defaultSpawnPosition = position.defaultSpawnPosition(seed)

        IslandsTable.update(where = { IslandsTable.id eq position.toLong() }) {
            it[IslandsTable.spawnOffsetX] = defaultSpawnPosition.blockX().toDouble() + 0.5
            it[IslandsTable.spawnOffsetY] = (IslandsWorld.SEA_LEVEL + 2).toDouble()
            it[IslandsTable.spawnOffsetZ] = defaultSpawnPosition.blockZ().toDouble() + 0.5
            it[IslandsTable.spawnYaw] = position.defaultSpawnYaw(seed)
        }
    }
}

internal data class IslandRecordInitResult(
    val position: IslandPosition,
    val initialized: Boolean,
)
