package net.azisaba.vanilife.island

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.azisaba.vanilife.ConfigurationHolder
import net.azisaba.vanilife.Vanilife
import net.azisaba.vanilife.island.event.IslandInitEvent
import net.azisaba.vanilife.island.event.IslandLoadEvent
import net.azisaba.vanilife.world.IslandPosition
import net.azisaba.vanilife.world.IslandsWorld
import org.bukkit.plugin.Plugin
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class IslandsAccessor internal constructor(
    private val config: ConfigurationHolder<Configuration>,
    private val database: Database,
    private val plugin: Plugin,
) {
    val islands: Collection<Island>
        get() = byPosition.values.toSet()

    private val byPosition: ConcurrentMap<IslandPosition, Island> = ConcurrentHashMap()
    private val byOwner: ConcurrentMap<UUID, Island> = ConcurrentHashMap()

    private val mutexMap: ConcurrentMap<IslandPosition, Mutex> = ConcurrentHashMap()

    fun byPosition(position: IslandPosition): Island? = byPosition[position]

    fun byOwner(owner: UUID): Island? = byOwner[owner]

    suspend fun load(position: IslandPosition, cause: IslandLoadEvent.Cause = IslandLoadEvent.Cause.UNKNOWN) {
        byPosition[position]?.let { return }

        val mutex = mutexMap.computeIfAbsent(position) { Mutex() }

        mutex.withLock {
            byPosition[position]?.let { return }

            val row = suspendTransaction(database) {
                IslandsTable.select(IslandsTable.owner)
                    .where { IslandsTable.id eq position.toLong() }
                    .singleOrNull()
            } ?: error("Island at $position does not exist")

            val island = Island(position, config.map(Configuration::wrack), database, plugin)
            val owner = row[IslandsTable.owner]

            try {
                island.bootstrap()
                IslandLoadEvent(island, cause).callEvent()

                byPosition[position] = island
                byOwner[owner] = island
            } catch (e: Throwable) {
                byPosition.remove(position)
                byOwner.remove(owner)
                throw e
            }
        }
    }

    suspend fun load(owner: UUID, cause: IslandLoadEvent.Cause = IslandLoadEvent.Cause.UNKNOWN) {
        if (owner in byOwner) return

        val positionId = suspendTransaction(database) {
            IslandsTable.select(IslandsTable.id)
                .where { IslandsTable.owner eq owner }
                .singleOrNull()
                ?.get(IslandsTable.id)
                ?.value
        } ?: error("Island not found for $owner")

        load(IslandPosition.fromLong(positionId), cause)
    }

    suspend fun create(owner: UUID) {
        val positionId = suspendTransaction(database) {
            IslandsTable.insertAndGetId {
                it[IslandsTable.owner] = owner
            }.value
        }

        val position = IslandPosition.fromLong(positionId)

        val seed = Vanilife.getIslandsWorld().seed
        val spawn = position.defaultSpawnPosition(seed).toCenter()

        suspendTransaction(database) {
            IslandsTable.update(where = { IslandsTable.id eq positionId }) {
                it[IslandsTable.spawnOffsetX] = spawn.x() - position.centerBlockX().toDouble()
                it[IslandsTable.spawnOffsetY] = spawn.y() - IslandsWorld.MIN_Y.toDouble()
                it[IslandsTable.spawnOffsetZ] = spawn.z() - position.centerBlockZ().toDouble()
            }
        }

        IslandInitEvent(position, owner).callEvent()
    }

    suspend fun createIfNotExists(owner: UUID): Boolean = try {
        create(owner)
        true
    } catch (_: Throwable) {
        false
    }
}
