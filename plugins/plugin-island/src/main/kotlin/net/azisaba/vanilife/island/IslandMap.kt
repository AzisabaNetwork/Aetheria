package net.azisaba.vanilife.island

import com.github.shynixn.mccoroutine.folia.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import net.azisaba.vanilife.world.IslandPosition
import org.bukkit.plugin.Plugin
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

internal class IslandMap(
    private val database: Database,
    private val plugin: Plugin,
    private val map: ConcurrentMap<IslandPosition, Island> = ConcurrentHashMap(),
) {
    private val positionByOwner: ConcurrentMap<UUID, IslandPosition> = ConcurrentHashMap()
    private var job: Job? = null

    init {
        job = plugin.launch {
            var time = 0L
            while (isActive) {
                tick(time++)
                delay(50L)
            }
        }
    }

    suspend fun lookup(position: IslandPosition): Island? {
        map[position]?.let { return it }
        val owner = lookupOwner(position) ?: return null
        positionByOwner.putIfAbsent(owner, position)
        return map.computeIfAbsent(position) { Island(it, owner, database) }
    }

    suspend fun lookup(owner: UUID): Island? {
        val position = lookupPosition(owner) ?: return null
        return lookup(position)
    }

    suspend fun lookupOrCreate(owner: UUID): Island = lookup(owner) ?: create(owner)

    suspend fun lookupPosition(owner: UUID): IslandPosition? = positionByOwner[owner] ?: suspendTransaction(database) {
        IslandsTable.select(IslandsTable.id)
            .where { IslandsTable.owner eq owner }
            .singleOrNull()
            ?.get(IslandsTable.id)
            ?.value
            ?.let(IslandPosition::fromLong)
    }?.also { positionByOwner.putIfAbsent(owner, it) }

    suspend fun contains(position: IslandPosition): Boolean = suspendTransaction(database) {
        IslandsTable.select(IslandsTable.id)
            .where { IslandsTable.id eq position.toLong() }
            .singleOrNull() != null
    }

    private suspend fun create(owner: UUID): Island {
        val position = suspendTransaction(database) {
            IslandsTable.insertAndGetId {
                it[IslandsTable.owner] = owner
                it[IslandsTable.level] = 1
                it[IslandsTable.displayName] = null
            }.value.let(IslandPosition::fromLong)
        }

        positionByOwner.putIfAbsent(owner, position)
        return map.computeIfAbsent(position) { Island(it, owner, database) }
    }

    private suspend fun lookupOwner(position: IslandPosition): UUID? = suspendTransaction(database) {
        IslandsTable.select(IslandsTable.owner)
            .where { IslandsTable.id eq position.toLong() }
            .singleOrNull()
            ?.get(IslandsTable.owner)
    }

    private suspend fun tick(time: Long) {
        for (island in map.values) {
            if (island.isActive()) {
                island.tick(time)
            }
        }
    }
}
