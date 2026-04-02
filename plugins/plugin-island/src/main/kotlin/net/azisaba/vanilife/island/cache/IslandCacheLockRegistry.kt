package net.azisaba.vanilife.island.cache

import kotlinx.coroutines.sync.Mutex
import net.azisaba.vanilife.world.IslandPosition
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

internal class IslandCacheLockRegistry {
    private val islandInitLocks: ConcurrentMap<IslandPosition, Mutex> = ConcurrentHashMap()
    private val ownerInitLocks: ConcurrentMap<UUID, Mutex> = ConcurrentHashMap()

    suspend fun <T> withIslandInitLock(position: IslandPosition, block: suspend () -> T): T {
        val mutex = islandInitLocks.computeIfAbsent(position) { Mutex() }
        mutex.lock()
        return try {
            block()
        } finally {
            mutex.unlock()
        }
    }

    suspend fun <T> withOwnerInitLock(owner: UUID, block: suspend () -> T): T {
        val mutex = ownerInitLocks.computeIfAbsent(owner) { Mutex() }
        mutex.lock()
        return try {
            block()
        } finally {
            mutex.unlock()
        }
    }
}
