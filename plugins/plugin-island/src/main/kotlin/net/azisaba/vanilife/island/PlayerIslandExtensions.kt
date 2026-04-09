package net.azisaba.vanilife.island

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.azisaba.vanilife.Vanilife
import net.azisaba.vanilife.island.lookup.OwnerLookup
import net.azisaba.vanilife.island.owner.PlayerIslandOwnersTable
import net.azisaba.vanilife.world.IslandPosition
import net.azisaba.vanilife.world.IslandsWorld
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

private val initializationLocks: ConcurrentMap<UUID, Mutex> = ConcurrentHashMap()

internal fun IslandSource.getPlayerIsland(ownerUuid: UUID): PlayerIsland? {
    val position = OwnerLookup[ownerUuid] ?: return null
    return get(position) as? PlayerIsland
}

internal suspend fun IslandSource.loadOrInitializePlayerIsland(ownerUuid: UUID, database: Database): PlayerIsland {
    OwnerLookup.getOrFind(ownerUuid, database)?.let {
        return loadIsland(it) as PlayerIsland
    }

    val position = IslandType.PLAYER.initialize(ownerUuid, database)
    return loadIsland(position) as PlayerIsland
}

internal suspend fun IslandType<PlayerIsland>.findPosition(ownerUuid: UUID, database: Database): IslandPosition? {
    val positionId = suspendTransaction(database) {
        PlayerIslandOwnersTable.select(PlayerIslandOwnersTable.position)
            .where { PlayerIslandOwnersTable.owner eq ownerUuid }
            .singleOrNull()
            ?.get(PlayerIslandOwnersTable.position)
            ?.value
    }
    return positionId?.let(IslandPosition::fromLong)
}

internal suspend fun IslandType<PlayerIsland>.initialize(ownerUuid: UUID, database: Database): IslandPosition {
    val mutex = initializationLocks.computeIfAbsent(ownerUuid) { Mutex() }

    return mutex.withLock {
        try {
            val positionId = suspendTransaction(database) {
                val entityId = IslandsTable.insertAndGetId {
                    it[IslandsTable.type] = this@initialize
                }

                PlayerIslandOwnersTable.insert {
                    it[PlayerIslandOwnersTable.position] = entityId
                    it[PlayerIslandOwnersTable.owner] = ownerUuid
                }

                entityId.value
            }
            val position = IslandPosition.fromLong(positionId)

            val spawnPoint = position.defaultSpawnPosition(Vanilife.getIslandsWorld().seed)

            suspendTransaction(database) {
                IslandsTable.update(where = { IslandsTable.id eq positionId }) {
                    it[IslandsTable.spawnOffsetX] = spawnPoint.x() - position.centerBlockX().toDouble()
                    it[IslandsTable.spawnOffsetY] = spawnPoint.y() - IslandsWorld.MIN_Y.toDouble()
                    it[IslandsTable.spawnOffsetZ] = spawnPoint.z() - position.centerBlockZ().toDouble()
                }
            }

            OwnerLookup.bind(ownerUuid, position)

            position
        } finally {
            initializationLocks.remove(ownerUuid, mutex)
        }
    }
}

