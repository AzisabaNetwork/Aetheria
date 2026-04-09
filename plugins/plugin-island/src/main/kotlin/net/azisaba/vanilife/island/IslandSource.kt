package net.azisaba.vanilife.island

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.azisaba.vanilife.ConfigurationHolder
import net.azisaba.vanilife.island.util.EnchantmentUnlockRates
import net.azisaba.vanilife.world.IslandPosition
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

internal class IslandSource(private val database: Database, private val config: ConfigurationHolder<Configuration>) {
    val loadedIslands: Collection<Island>
        get() = islands.values.toSet()

    private val islands: ConcurrentMap<IslandPosition, Island> = ConcurrentHashMap()

    private val loadingLocks: ConcurrentMap<IslandPosition, Mutex> = ConcurrentHashMap()

    operator fun get(position: IslandPosition): Island? = islands[position]

    suspend fun getOrLoad(position: IslandPosition): Island? = get(position) ?: loadIsland(position)

    operator fun contains(position: IslandPosition): Boolean = position in islands

    @Suppress("UNCHECKED_CAST")
    suspend fun tickIslands(time: Long) {
        for (island in islands.values) {
            val ticker = island.type.ticker as? IslandTicker<Island>
            ticker?.tick(island, time)
        }
    }

    suspend fun loadIsland(position: IslandPosition): Island? {
        requireNotLoaded(position)

        val mutex = loadingLocks.computeIfAbsent(position) { Mutex() }

        return mutex.withLock {
            try {
                requireNotLoaded(position)

                val islandType = suspendTransaction(database) {
                    IslandsTable.select(IslandsTable.type)
                        .where { IslandsTable.id eq position.toLong() }
                        .singleOrNull()
                        ?.get(IslandsTable.type)
                } ?: return@withLock null

                val island = islandType.instantiate(position, database, config).apply {
                    bootstrap()
                }

                EnchantmentUnlockRates.handleIslandInit()

                islands[position] = island
                island
            } finally {
                loadingLocks.remove(position, mutex)
            }
        }
    }

    private fun requireNotLoaded(position: IslandPosition) = require(position !in islands) {
        "Island $position is already loaded"
    }
}
