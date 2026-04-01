package net.azisaba.vanilife.island

import net.azisaba.vanilife.ConfigurationHolder
import net.azisaba.vanilife.Vanilife
import net.azisaba.vanilife.island.wrack.WrackConfiguration
import net.azisaba.vanilife.world.IslandPosition
import net.azisaba.vanilife.world.IslandsWorld
import org.bukkit.plugin.Plugin
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

internal class IslandCacheMap(
    private val database: Database,
    private val plugin: Plugin,
    private val wrackConfig: ConfigurationHolder<WrackConfiguration>,
) : Iterable<Island> {
    private val islandByPosition: ConcurrentMap<IslandPosition, Island> = ConcurrentHashMap()

    private val positionByOwner: ConcurrentMap<UUID, IslandPosition> = ConcurrentHashMap()
    private val ownerByPosition: ConcurrentMap<IslandPosition, UUID> = ConcurrentHashMap()

    suspend fun lookup(position: IslandPosition): Island? {
        islandByPosition[position]?.let { return it }
        val row = suspendTransaction(database) {
            IslandsTable.selectAll()
                .where { IslandsTable.id eq position.toLong() }
                .singleOrNull()
        } ?: return null

        val owner = row[IslandsTable.owner]
        val level = row[IslandsTable.level]

        val island = islandByPosition.computeIfAbsent(position) {
            Island(
                position = it,
                owner,
                level,
                score = row[IslandsTable.score],
                displayName = row[IslandsTable.displayName],
                spawnOffsetX = row[IslandsTable.spawnOffsetX],
                spawnOffsetY = row[IslandsTable.spawnOffsetY],
                spawnOffsetZ = row[IslandsTable.spawnOffsetZ],
                spawnYaw = row[IslandsTable.spawnYaw],
                spawnPitch = row[IslandsTable.spawnPitch],
                spawnLimit = wrackConfig.map(WrackConfiguration::spawnLimit),
                spawnIntervalTicks = wrackConfig.map(WrackConfiguration::spawnIntervalTicks),
                database,
                plugin,
            )
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
        val levelSeed = Vanilife.getIslandsWorld().seed
        val spawnBlock = position.spawnBlock(levelSeed)
        val spawnYaw = position.spawnYaw(levelSeed)
        cacheOwnerPosition(owner, position)
        return islandByPosition.computeIfAbsent(position) {
            Island(
                it,
                owner,
                Island.MIN_LEVEL,
                score = 0.0,
                displayName = null,
                spawnOffsetX = spawnBlock.blockX().toDouble() + 0.5,
                spawnOffsetY = (IslandsWorld.SEA_LEVEL + 2).toDouble(),
                spawnOffsetZ = spawnBlock.blockZ().toDouble() + 0.5,
                spawnYaw = spawnYaw,
                spawnPitch = 0f,
                spawnLimit = wrackConfig.map(WrackConfiguration::spawnLimit),
                spawnIntervalTicks = wrackConfig.map(WrackConfiguration::spawnIntervalTicks),
                database,
                plugin,
            )
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
        val id = IslandsTable.insertAndGetId {
            it[IslandsTable.owner] = owner
            it[IslandsTable.level] = Island.MIN_LEVEL
            it[IslandsTable.score] = 0.0
            it[IslandsTable.displayName] = null
            it[IslandsTable.spawnOffsetX] = 0.0
            it[IslandsTable.spawnOffsetY] = 0.0
            it[IslandsTable.spawnOffsetZ] = 0.0
            it[IslandsTable.spawnYaw] = 0f
            it[IslandsTable.spawnPitch] = 0f
        }
        val position = IslandPosition.fromLong(id.value)
        val levelSeed = Vanilife.getIslandsWorld().seed
        val spawnBlock = position.spawnBlock(levelSeed)
        val spawnYaw = position.spawnYaw(levelSeed)

        IslandsTable.update(where = { IslandsTable.id eq position.toLong() }) {
            it[IslandsTable.spawnOffsetX] = spawnBlock.blockX().toDouble() + 0.5
            it[IslandsTable.spawnOffsetY] = (IslandsWorld.SEA_LEVEL + 2).toDouble()
            it[IslandsTable.spawnOffsetZ] = spawnBlock.blockZ().toDouble() + 0.5
            it[IslandsTable.spawnYaw] = spawnYaw
        }

        position
    }
}
