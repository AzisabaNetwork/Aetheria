package net.azisaba.vanilife.island.loader

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.azisaba.vanilife.ConfigurationHolder
import net.azisaba.vanilife.island.IslandsAccessor
import net.azisaba.vanilife.island.IslandsTable
import net.azisaba.vanilife.island.event.IslandLoadEvent
import net.azisaba.vanilife.island.visitors.IslandVisitorsTable
import net.azisaba.vanilife.world.IslandPosition
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import kotlin.time.Clock
import kotlin.time.Instant

internal class IslandLoader(
    private val islands: IslandsAccessor,
    private val config: ConfigurationHolder<LoaderConfiguration>,
    private val database: Database,
) : AutoCloseable {
    private val mutex = Mutex()
    private val queue = ArrayDeque<IslandPosition>()
    private val queued = LinkedHashSet<IslandPosition>()
    private var closed = false

    suspend fun tick() {
        if (closed) return

        val config = config.value()
        if (!config.enabled) {
            clearQueue()
            return
        }

        fillQueue(config)
        loadQueued(config)
    }

    override fun close() {
        closed = true
    }

    private suspend fun fillQueue(config: LoaderConfiguration) {
        val capacity = config.queueCapacity.coerceAtLeast(0)
        if (capacity == 0) return

        val remaining = mutex.withLock { capacity - queue.size }
        if (remaining <= 0) return

        val candidates = findAutoLoadTargets(
            minLevel = config.minLevel,
            since = Clock.System.now() - config.maxIdle,
            limit = remaining,
        )

        candidates.forEach { position ->
            mutex.withLock {
                if (queue.size >= capacity) return
                if (position in queued) return@withLock
                if (islands.byPosition(position) != null) return@withLock

                queue.addLast(position)
                queued.add(position)
            }
        }
    }

    private suspend fun loadQueued(config: LoaderConfiguration) {
        repeat(config.loadPerTick.coerceAtLeast(0)) {
            val position = mutex.withLock {
                val next = queue.removeFirstOrNull() ?: return
                queued.remove(next)
                next
            }

            if (islands.byPosition(position) == null) {
                islands.load(position, IslandLoadEvent.Cause.LOADER)
            }
        }
    }

    private suspend fun clearQueue() {
        mutex.withLock {
            queue.clear()
            queued.clear()
        }
    }

    private suspend fun findAutoLoadTargets(minLevel: Int, since: Instant, limit: Int): List<IslandPosition> =
        suspendTransaction(database) {
            if (limit <= 0) return@suspendTransaction emptyList()

            val result = ArrayList<IslandPosition>(limit)
            val seen = HashSet<Long>(limit)

            IslandVisitorsTable
                .select(IslandVisitorsTable.position, IslandVisitorsTable.lastVisitAt)
                .where { IslandVisitorsTable.lastVisitAt greaterEq since }
                .orderBy(IslandVisitorsTable.lastVisitAt, SortOrder.DESC)
                .forEach { row ->
                    val positionId = row[IslandVisitorsTable.position].value
                    if (!seen.add(positionId)) return@forEach

                    val level = IslandsTable
                        .select(IslandsTable.level)
                        .where { IslandsTable.id eq positionId }
                        .singleOrNull()
                        ?.get(IslandsTable.level) ?: return@forEach

                    if (level < minLevel) return@forEach

                    result.add(IslandPosition.fromLong(positionId))
                    if (result.size >= limit) return@suspendTransaction result
                }

            result
        }
}
