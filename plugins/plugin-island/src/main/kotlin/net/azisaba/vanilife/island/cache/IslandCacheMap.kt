package net.azisaba.vanilife.island.cache

import net.azisaba.vanilife.ConfigurationHolder
import net.azisaba.vanilife.island.Island
import net.azisaba.vanilife.island.event.IslandInitEvent
import net.azisaba.vanilife.island.wrack.WrackConfiguration
import net.azisaba.vanilife.world.IslandPosition
import org.bukkit.plugin.Plugin
import org.jetbrains.exposed.v1.jdbc.Database
import java.util.*

internal class IslandCacheMap(
    private val database: Database,
    private val plugin: Plugin,
    private val wrackConfig: ConfigurationHolder<WrackConfiguration>,
) : Iterable<Island> {
    private val lockRegistry = IslandCacheLockRegistry()
    private val ownerIndex = IslandOwnerIndex()
    private val instanceRegistry = IslandInstanceRegistry()
    private val ownershipRepository = IslandOwnershipRepository(database)

    suspend fun lookup(position: IslandPosition): Island? {
        instanceRegistry.lookup(position)?.let { return it }

        return lockRegistry.withIslandInitLock(position) {
            instanceRegistry.lookup(position)?.let { return@withIslandInitLock it }

            val owner = ownershipRepository.lookupOwner(position) ?: return@withIslandInitLock null
            val island = initIsland(position)

            instanceRegistry.cache(position, island)
            ownerIndex.cache(owner, position)
            island
        }
    }

    suspend fun lookup(owner: UUID): Island? {
        val position = lookupPosition(owner) ?: return null
        return lookup(position)
    }

    suspend fun lookupOrInit(owner: UUID): Island {
        lookup(owner)?.let { return it }

        return lockRegistry.withOwnerInitLock(owner) {
            lookup(owner)?.let { return@withOwnerInitLock it }

            val initResult = ownershipRepository.initRecordIfMissing(owner)
            ownerIndex.cache(owner, initResult.position)

            val island = requireNotNull(lookup(initResult.position)) {
                "Island row exists for owner=$owner but lookup returned null"
            }

            if (initResult.initialized) {
                IslandInitEvent(island).callEvent()
            }

            island
        }
    }

    suspend fun lookupPosition(owner: UUID): IslandPosition? {
        ownerIndex.lookupPosition(owner)?.let { return it }
        val position = ownershipRepository.lookupPosition(owner) ?: return null
        ownerIndex.cache(owner, position)
        return position
    }

    suspend fun lookupOwner(position: IslandPosition): UUID? {
        ownerIndex.lookupOwner(position)?.let { return it }
        val owner = ownershipRepository.lookupOwner(position) ?: return null
        ownerIndex.cache(owner, position)
        return owner
    }

    suspend fun contains(position: IslandPosition): Boolean = ownershipRepository.contains(position)

    override fun iterator(): Iterator<Island> = instanceRegistry.iterator()

    private suspend fun initIsland(position: IslandPosition): Island = Island(
        position = position,
        wrackSpawnLimit = wrackConfig.map(WrackConfiguration::spawnLimit),
        wrackSpawnIntervalTicks = wrackConfig.map(WrackConfiguration::spawnIntervalTicks),
        database,
        plugin,
    ).also { it.bootstrap() }
}
