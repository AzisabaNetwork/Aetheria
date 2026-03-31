package net.azisaba.vanilife.island

import net.azisaba.vanilife.world.IslandPosition
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

internal class IslandCacheMap(private val database: Database) : Iterable<Island> {
    private val islandByPosition: ConcurrentMap<IslandPosition, Island> = ConcurrentHashMap()

    private val positionByOwner: ConcurrentMap<UUID, IslandPosition> = ConcurrentHashMap()
    private val ownerByPosition: ConcurrentMap<IslandPosition, UUID> = ConcurrentHashMap()

    suspend fun lookup(position: IslandPosition): Island? {
        islandByPosition[position]?.let { return it }
        val row = suspendTransaction(database) {
            IslandsTable.select(IslandsTable.owner, IslandsTable.level, IslandsTable.score, IslandsTable.displayName)
                .where { IslandsTable.id eq position.toLong() }
                .singleOrNull()
        } ?: return null

        val owner = row[IslandsTable.owner]
        val level = row[IslandsTable.level]
        val score = row[IslandsTable.score]
        val displayName = row[IslandsTable.displayName]

        val island = islandByPosition.computeIfAbsent(position) {
            Island(it, owner, level, score, displayName, database)
        }
        cacheOwnerPosition(owner, position)
        return island
    }

    suspend fun lookup(owner: UUID): Island? {
        val position = lookupPosition(owner) ?: return null
        return lookup(position)
    }

    suspend fun lookupOrCreate(owner: UUID): Island {
        lookup(owner)?.let { return it }

        val position = insertToDatabase(owner)
        cacheOwnerPosition(owner, position)
        return islandByPosition.computeIfAbsent(position) {
            Island(it, owner, Island.MIN_LEVEL, 0.0, null, database)
        }
    }

    suspend fun lookupPosition(owner: UUID): IslandPosition? {
        positionByOwner[owner]?.let { return it }
        val position = lookupPositionInDatabase(owner) ?: return null
        cacheOwnerPosition(owner, position)
        return position
    }

    suspend fun lookupOwner(position: IslandPosition): UUID? {
        ownerByPosition[position]?.let { return it }
        val owner = lookupOwnerInDatabase(position) ?: return null
        cacheOwnerPosition(owner, position)
        return owner
    }

    suspend fun contains(position: IslandPosition): Boolean = suspendTransaction(database) {
        IslandsTable.select(IslandsTable.id)
            .where { IslandsTable.id eq position.toLong() }
            .singleOrNull() != null
    }

    override fun iterator(): Iterator<Island> = islandByPosition.values.iterator()

    private fun cacheOwnerPosition(owner: UUID, position: IslandPosition) {
        positionByOwner.putIfAbsent(owner, position)
        ownerByPosition.putIfAbsent(position, owner)
    }

    private suspend fun lookupOwnerInDatabase(position: IslandPosition): UUID? = suspendTransaction(database) {
        IslandsTable.select(IslandsTable.owner)
            .where { IslandsTable.id eq position.toLong() }
            .singleOrNull()
            ?.get(IslandsTable.owner)
    }

    private suspend fun lookupPositionInDatabase(owner: UUID): IslandPosition? = suspendTransaction(database) {
        IslandsTable.select(IslandsTable.id)
            .where { IslandsTable.owner eq owner }
            .singleOrNull()
            ?.get(IslandsTable.id)
            ?.value
            ?.let(IslandPosition::fromLong)
    }

    private suspend fun insertToDatabase(owner: UUID): IslandPosition = suspendTransaction(database) {
        IslandsTable.insertAndGetId {
            it[IslandsTable.owner] = owner
            it[IslandsTable.level] = Island.MIN_LEVEL
            it[IslandsTable.score] = 0.0
            it[IslandsTable.displayName] = null
        }.value.let(IslandPosition::fromLong)
    }
}
