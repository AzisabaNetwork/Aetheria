package net.azisaba.vanilife.island

import com.github.shynixn.mccoroutine.folia.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import net.azisaba.vanilife.world.IslandPos
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
    private val map: ConcurrentMap<IslandPos, Island> = ConcurrentHashMap(),
) {
    private val positionByOwner: ConcurrentMap<UUID, IslandPos> = ConcurrentHashMap()
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

    suspend fun lookup(position: IslandPos): Island? {
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

    suspend fun lookupPosition(owner: UUID): IslandPos? = positionByOwner[owner] ?: suspendTransaction(database) {
        IslandsTable.select(IslandsTable.id)
            .where { IslandsTable.owner eq owner }
            .singleOrNull()
            ?.get(IslandsTable.id)
            ?.value
            ?.let(IslandPos::fromLong)
    }?.also { positionByOwner.putIfAbsent(owner, it) }

    suspend fun contains(position: IslandPos): Boolean = suspendTransaction(database) {
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
            }.value.let(IslandPos::fromLong)
        }

        positionByOwner.putIfAbsent(owner, position)
        return map.computeIfAbsent(position) { Island(it, owner, database) }
    }

    private suspend fun lookupOwner(position: IslandPos): UUID? = suspendTransaction(database) {
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
